package com.traceback;

import static com.traceback.Models.*;
import static org.springframework.http.HttpStatus.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RunService {
  private final RunRepository runs;
  private final ArtifactRepository artifacts;
  private final ObjectMapper json;
  private final Simulator simulator;

  public RunService(RunRepository r, ArtifactRepository a, ObjectMapper j, Simulator s) {
    runs = r;
    artifacts = a;
    json = j;
    simulator = s;
  }

  public Run create(Scenario scenario) {
    Run run = simulator.simulate(UUID.randomUUID(), scenario, Instant.now());
    var entity = new Entities.RunEntity();
    entity.id = run.id();
    entity.scenario = scenario.name();
    entity.createdAt = run.createdAt();
    entity.status = run.status();
    for (var event : run.events()) {
      var e = new Entities.EventEntity();
      e.id = event.id();
      e.sequence = event.sequence();
      e.payload = encode(event);
      entity.events.add(e);
    }
    for (var order : run.orders()) {
      var o = new Entities.OrderEntity();
      o.id = order.id();
      o.logicalOrderId = order.logicalOrderId();
      o.idempotencyKey = order.idempotencyKey();
      o.status = order.status();
      entity.orders.add(o);
    }
    runs.save(entity);
    return run;
  }

  public Entities.RunEntity entity(UUID id) {
    return runs.findById(id)
        .orElseThrow(
            () -> new ApiException(NOT_FOUND, "RUN_NOT_FOUND", "Simulation run was not found"));
  }

  @Transactional(readOnly = true)
  public Run get(UUID id) {
    return dto(entity(id));
  }

  private Run dto(Entities.RunEntity e) {
    return new Run(
        e.id,
        Scenario.valueOf(e.scenario),
        e.createdAt,
        e.status,
        e.events.stream().map(x -> decode(x.payload, Event.class)).toList(),
        e.orders.stream()
            .sorted(Comparator.comparing(x -> x.id))
            .map(x -> new Order(x.id, x.logicalOrderId, x.idempotencyKey, x.status))
            .toList());
  }

  @Transactional(readOnly = true)
  public Models.Page<Summary> list(int page, int size) {
    var result =
        runs.findAll(
            PageRequest.of(
                page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id"))));
    return new Models.Page<>(
        result.getContent().stream().map(e -> summary(dto(e))).toList(),
        result.getTotalElements(),
        page,
        size,
        result.isLast());
  }

  private Summary summary(Run r) {
    return new Summary(
        r.id(),
        r.scenario(),
        r.createdAt(),
        r.status(),
        r.events().size(),
        r.orders().size(),
        simulator.metrics(r).duplicateCount(),
        latest(r.id(), "INVESTIGATION", Investigation.class)
            .map(Investigation::status)
            .orElse("NOT_RUN"),
        latest(r.id(), "VERIFICATION", Verification.class)
            .map(Verification::status)
            .orElse("NOT_RUN"));
  }

  @Transactional(readOnly = true)
  public Map<String, Object> stats() {
    // Aggregate all persisted runs, independently of the history page shown in the UI.
    long duplicates = 0, verified = 0, passed = 0;
    for (var e : runs.findAll()) {
      duplicates += simulator.metrics(dto(e)).duplicateCount();
      var v = latest(e.id, "VERIFICATION", Verification.class);
      if (v.isPresent()) {
        verified++;
        if (v.get().status().equals("PASSED")) passed++;
      }
    }
    return Map.of(
        "totalRuns",
        runs.count(),
        "totalInvestigations",
        artifacts.countByKind("INVESTIGATION"),
        "failedSimulations",
        runs.countByStatus("FAILED"),
        "duplicateOrders",
        duplicates,
        "verifiedRuns",
        verified,
        "verificationPassRate",
        verified == 0 ? -1.0 : (100.0 * passed / verified));
  }

  public Verification verify(UUID id) {
    var result = simulator.verify(get(id));
    saveArtifact(id, "VERIFICATION", result.id(), result);
    return result;
  }

  public void saveArtifact(UUID runId, String kind, UUID id, Object result) {
    var a = new Entities.Artifact();
    a.id = id;
    a.run = entity(runId);
    a.kind = kind;
    a.createdAt = Instant.now();
    a.payload = encode(result);
    artifacts.save(a);
  }

  @Transactional(readOnly = true)
  public <T> Optional<T> latest(UUID id, String kind, Class<T> type) {
    entity(id);
    return artifacts
        .findFirstByRunIdAndKindOrderByCreatedAtDesc(id, kind)
        .map(a -> decode(a.payload, type));
  }

  String encode(Object value) {
    try {
      return json.writeValueAsString(value);
    } catch (Exception e) {
      throw new IllegalStateException("Could not serialize stored data");
    }
  }

  <T> T decode(String value, Class<T> type) {
    try {
      return json.readValue(value, type);
    } catch (Exception e) {
      throw new IllegalStateException("Could not read stored data");
    }
  }
}
