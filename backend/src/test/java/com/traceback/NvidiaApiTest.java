package com.traceback;

import static com.traceback.Models.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:nvidia-tests;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
      "traceback.ai.provider=nvidia",
      "traceback.nvidia.key="
    })
@AutoConfigureMockMvc
class NvidiaApiTest {
  @Autowired MockMvc mvc;
  @Autowired RunService runs;
  @Autowired ObjectMapper json;
  @MockitoBean NvidiaProvider provider;

  @Test
  void storesActualHostAndModelAndCachesValidatedAnalysis() throws Exception {
    var run = runs.create(Scenario.DUPLICATE_ORDER);
    when(provider.model()).thenReturn("meta/muse-glimmer-30b");
    when(provider.complete(anyString()))
        .thenReturn(
            json.writeValueAsString(
                new Analysis(
                    "Unsafe retry",
                    "Two order commits",
                    List.of(run.events().get(6).id()),
                    "Stored duplicate",
                    "No retry protection",
                    List.of(),
                    List.of(),
                    "Controlled simulation only",
                    "Reuse stable key",
                    "Replay with idempotency",
                    List.of("No production changes"))));
    mvc.perform(post("/api/investigations/" + run.id()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.provider").value("nvidia"))
        .andExpect(jsonPath("$.model").value("meta/muse-glimmer-30b"));
    mvc.perform(post("/api/investigations/" + run.id())).andExpect(status().isOk());
    verify(provider, times(1)).complete(anyString());
  }

  @Test
  void healthHasProviderNeutralStatusAndNoSecret() throws Exception {
    var safe =
        Map.<String, Object>of(
            "provider",
            "nvidia",
            "model",
            "meta/muse-glimmer-30b",
            "configured",
            false,
            "status",
            "NOT_CONFIGURED",
            "message",
            "Set NVIDIA_API_KEY");
    when(provider.status()).thenReturn(safe);
    mvc.perform(get("/api/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ai.provider").value("nvidia"))
        .andExpect(jsonPath("$.ai.configured").value(false));
  }
}
