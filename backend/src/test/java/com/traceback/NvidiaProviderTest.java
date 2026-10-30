package com.traceback;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class NvidiaProviderTest {
  final ObjectMapper json = new ObjectMapper();

  NvidiaProvider provider(String key) {
    return new NvidiaProvider(key, "meta/muse-glimmer-30b", json);
  }

  @Test
  void missingKeyDoesNotCallNetwork() {
    assertThatThrownBy(() -> provider("").complete("trace"))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("NVIDIA_API_KEY");
  }

  @Test
  void statusNeverContainsSecret() {
    var p = provider("test-only-secret");
    assertThat(p.status().get("configured")).isEqualTo(true);
    assertThat(p.status().toString()).doesNotContain("test-only-secret");
  }

  @Test
  void documentedRequestUsesSystemAndMaxTokens() throws Exception {
    var body = provider("").requestBody("actual trace");
    var node = json.valueToTree(body);
    assertThat(node.path("model").asText()).isEqualTo("meta/muse-glimmer-30b");
    assertThat(node.path("max_tokens").asInt()).isEqualTo(2000);
    assertThat(node.at("/messages/0/role").asText()).isEqualTo("system");
    assertThat(node.at("/messages/1/content").asText()).isEqualTo("actual trace");
    assertThat(node.path("reasoning_effort").asText()).isEqualTo("none");
    assertThat(node.path("temperature").asDouble()).isEqualTo(0.95);
    assertThat(node.has("max_completion_tokens")).isFalse();
    assertThat(node.has("response_format")).isFalse();
  }

  @Test
  void completeResponseExtractsOnlyContent() {
    assertThat(
            provider("")
                .readCompletion(
                    "{\"choices\":[{\"finish_reason\":\"stop\",\"message\":{\"content\":\"{\\\"answer\\\":true}\"}}]}"))
        .isEqualTo("{\"answer\":true}");
  }

  @Test
  void truncatedAndMalformedResponsesAreRejected() {
    assertThatThrownBy(
            () -> provider("").readCompletion("{\"choices\":[{\"finish_reason\":\"length\"}]}"))
        .isInstanceOf(ApiException.class);
    assertThatThrownBy(() -> provider("").readCompletion("invalid json"))
        .isInstanceOf(ApiException.class);
  }

  @Test
  void providerErrorsAreSafeAndSpecific() {
    var p = provider("private");
    assertThat(p.responseError(401).getMessage()).contains("key").doesNotContain("private");
    assertThat(p.responseError(429).status).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    assertThat(p.responseError(202).getMessage()).contains("does not poll");
  }

  @Test
  void routerSelectsNvidiaWithoutCallingMeta() {
    var m = mock(MetaProvider.class);
    var n = mock(NvidiaProvider.class);
    when(n.complete("trace")).thenReturn("result");
    when(n.model()).thenReturn("meta/muse-glimmer-30b");
    var router = new AiProviderRouter("nvidia", "direct-meta-model", m, n);
    assertThat(router.complete("trace")).isEqualTo("result");
    assertThat(router.name()).isEqualTo("nvidia");
    assertThat(router.model()).isEqualTo("meta/muse-glimmer-30b");
    verifyNoInteractions(m);
  }

  @Test
  void unknownProviderDoesNotUseEitherKey() {
    var m = mock(MetaProvider.class);
    var n = mock(NvidiaProvider.class);
    var router = new AiProviderRouter("unknown", "model", m, n);
    assertThat(router.status().get("configured")).isEqualTo(false);
    assertThatThrownBy(() -> router.complete("trace")).isInstanceOf(ApiException.class);
    verifyNoInteractions(m, n);
  }
}
