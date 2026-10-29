package com.traceback;

import static com.traceback.Models.*;
import static org.springframework.http.HttpStatus.*;

import com.fasterxml.jackson.databind.*;
import java.time.Instant;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class InvestigationService {
  private final RunService runs;
  private final InvestigationProvider provider;
  private final ObjectMapper json;
  private final String model;

  public InvestigationService(
      RunService r,
      InvestigationProvider p,
      ObjectMapper j,
      @Value("${traceback.meta.model}") String m) {
    runs = r;
    provider = p;
    json = j;
    model = m;
  }

  public synchronized Investigation investigate(UUID id) {
    Run run = runs.get(id);
    var existing = runs.latest(id, "INVESTIGATION", Investigation.class);
    if (existing.isPresent()) return existing.get();
    String prompt = runs.encode(run);
    if (prompt.length() > 24000 || run.events().size() > 64)
      throw new ApiException(
          UNPROCESSABLE_ENTITY,
          "TRACE_TOO_LARGE",
          "Trace is too large for bounded AI investigation.");
    Analysis analysis = validate(provider.complete(prompt), run);
    var result =
        new Investigation(
            UUID.randomUUID(), id, Instant.now(), "COMPLETED", "meta", model, analysis);
    runs.saveArtifact(id, "INVESTIGATION", result.id(), result);
    return result;
  }

  Analysis validate(String response, Run run) {
    try {
      if (response == null || response.length() > 32000) throw new IllegalArgumentException();
      JsonNode tree = json.readTree(response);
      for (String name :
          List.of(
              "rootCauseHypothesis",
              "causalExplanation",
              "directEvidence",
              "inference",
              "uncertainty",
              "recommendedCorrection",
              "verificationPlan"))
        if (!tree.path(name).isTextual()
            || tree.path(name).asText().isBlank()
            || tree.path(name).asText().length() > 6000) throw new IllegalArgumentException();
      for (String name :
          List.of(
              "evidenceEventIds",
              "alternativeExplanations",
              "missingInformation",
              "risksAndAssumptions")) {
        if (!tree.path(name).isArray() || tree.path(name).size() > 32)
          throw new IllegalArgumentException();
        for (var value : tree.path(name))
          if (!value.isTextual() || value.asText().length() > 2000)
            throw new IllegalArgumentException();
      }
      Analysis a = json.treeToValue(tree, Analysis.class);
      var valid = new HashSet<>(run.events().stream().map(Event::id).toList());
      if (a.evidenceEventIds().isEmpty() || !valid.containsAll(a.evidenceEventIds()))
        throw new ApiException(
            BAD_GATEWAY,
            "INVALID_EVIDENCE",
            "Meta referenced evidence outside this run. Analysis was rejected and was not saved.");
      return a;
    } catch (ApiException e) {
      throw e;
    } catch (Exception e) {
      throw new ApiException(
          BAD_GATEWAY,
          "MALFORMED_ANALYSIS",
          "Meta returned invalid analysis. It was rejected and was not saved.");
    }
  }
}
