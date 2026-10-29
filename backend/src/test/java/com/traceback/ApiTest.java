package com.traceback;

import static com.traceback.Models.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:tests;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
      "traceback.meta.key="
    })
@AutoConfigureMockMvc
class ApiTest {
  @Autowired MockMvc mvc;
  @Autowired RunService runs;
  @Autowired ObjectMapper json;
  @Autowired InvestigationService service;
  @MockitoBean MetaProvider provider;

  @Test
  void creationAndPersistedHistory() throws Exception {
    var response =
        mvc.perform(
                post("/api/simulations/run")
                    .contentType("application/json")
                    .content("{\"scenario\":\"DUPLICATE_ORDER\"}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse();
    String id = json.readTree(response.getContentAsString()).path("id").asText();
    mvc.perform(get("/api/simulations/" + id + "/events"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(9));
    mvc.perform(get("/api/simulations/" + id + "/orders"))
        .andExpect(jsonPath("$.length()").value(2));
    mvc.perform(get("/api/simulations?size=1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1));
  }

  @Test
  void invalidScenarioRejected() throws Exception {
    mvc.perform(
            post("/api/simulations/run")
                .contentType("application/json")
                .content("{\"scenario\":\"FAKE\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
  }

  @Test
  void missingScenarioRejected() throws Exception {
    mvc.perform(post("/api/simulations/run").contentType("application/json").content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void paginationIsBounded() throws Exception {
    mvc.perform(get("/api/simulations?size=101")).andExpect(status().isBadRequest());
    mvc.perform(get("/api/simulations?page=-1")).andExpect(status().isBadRequest());
  }

  @Test
  void unknownRunAndMalformedId() throws Exception {
    mvc.perform(get("/api/simulations/" + UUID.randomUUID())).andExpect(status().isNotFound());
    mvc.perform(get("/api/simulations/nope")).andExpect(status().isBadRequest());
  }

  @Test
  void verificationPersisted() throws Exception {
    var r = runs.create(Scenario.DUPLICATE_ORDER);
    mvc.perform(post("/api/verification/" + r.id()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.before.ordersCreated").value(2))
        .andExpect(jsonPath("$.after.ordersCreated").value(1))
        .andExpect(jsonPath("$.status").value("PASSED"));
    mvc.perform(get("/api/verification/" + r.id())).andExpect(status().isOk());
  }

  @Test
  void noAnalysisReturnsNoContent() throws Exception {
    var r = runs.create(Scenario.NORMAL_ORDER);
    mvc.perform(get("/api/investigations/" + r.id())).andExpect(status().isNoContent());
  }

  String analysis(UUID evidence) throws Exception {
    return json.writeValueAsString(
        new Analysis(
            "Retry duplicated the order",
            "Stored orders share one logical ID",
            List.of(evidence),
            "Two order events",
            "Missing retry protection",
            List.of("Timeout ambiguity"),
            List.of(),
            "Controlled trace only",
            "Reuse idempotency key",
            "Replay and check count",
            List.of("Simulation scope")));
  }

  @Test
  void realEvidenceAcceptedAndSavedWithCallsMocked() throws Exception {
    var r = runs.create(Scenario.DUPLICATE_ORDER);
    when(provider.complete(anyString())).thenReturn(analysis(r.events().get(6).id()));
    mvc.perform(post("/api/investigations/" + r.id()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.analysis.rootCauseHypothesis").value("Retry duplicated the order"));
    mvc.perform(post("/api/investigations/" + r.id())).andExpect(status().isOk());
    verify(provider, times(1)).complete(anyString());
  }

  @Test
  void foreignEvidenceRejectedWithoutPersistence() throws Exception {
    var r = runs.create(Scenario.DUPLICATE_ORDER);
    var other = runs.create(Scenario.NORMAL_ORDER);
    when(provider.complete(anyString())).thenReturn(analysis(other.events().get(0).id()));
    mvc.perform(post("/api/investigations/" + r.id()))
        .andExpect(status().isBadGateway())
        .andExpect(jsonPath("$.code").value("INVALID_EVIDENCE"));
    assertThat(runs.latest(r.id(), "INVESTIGATION", Investigation.class)).isEmpty();
  }

  @Test
  void malformedAnalysisRejected() throws Exception {
    var r = runs.create(Scenario.NORMAL_ORDER);
    when(provider.complete(anyString())).thenReturn("not json");
    mvc.perform(post("/api/investigations/" + r.id()))
        .andExpect(status().isBadGateway())
        .andExpect(jsonPath("$.code").value("MALFORMED_ANALYSIS"));
  }

  @Test
  void missingKeyWithoutNetworkCall() {
    var p =
        new MetaProvider(
            "", "muse-spark-1.3", "meta", "https://api.meta.ai/v1/chat/completions", json);
    assertThatThrownBy(() -> p.complete("trace"))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("not configured");
    assertThat(p.status().toString()).doesNotContain("Bearer");
  }
}
