package com.traceback;

import static org.assertj.core.api.Assertions.*;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;

class DemoAccessFilterTest {
  private static final String PASSWORD = "test-only-password-long";
  private MockHttpServletResponse request(String method, String path, String auth, String origin) throws Exception {
    var req = new MockHttpServletRequest(method, path);
    if (auth != null) req.addHeader("Authorization", auth);
    if (origin != null) req.addHeader("Origin", origin);
    var res = new MockHttpServletResponse();
    new DemoAccessFilter(true, "judge", PASSWORD).doFilter(req, res, (a,b) -> ((jakarta.servlet.http.HttpServletResponse)b).setStatus(204));
    return res;
  }
  private String auth() { return "Basic " + Base64.getEncoder().encodeToString(("judge:" + PASSWORD).getBytes(java.nio.charset.StandardCharsets.UTF_8)); }
  @Test void rejectsMissingAndMalformedCredentials() throws Exception {
    assertThat(request("GET", "/", null, null).getStatus()).isEqualTo(401);
    var invalid = request("POST", "/api/simulations/run", "Basic !!!", null);
    assertThat(invalid.getStatus()).isEqualTo(401);
    assertThat(invalid.getContentAsString()).doesNotContain(PASSWORD);
  }
  @Test void protectsAssetsAndPaidEndpoints() throws Exception {
    assertThat(request("GET", "/assets/app.js", null, null).getStatus()).isEqualTo(401);
    assertThat(request("POST", "/api/investigations/anything", null, null).getStatus()).isEqualTo(401);
    assertThat(request("GET", "/", auth(), null).getStatus()).isEqualTo(204);
  }
  @Test void blocksCrossOriginChanges() throws Exception {
    assertThat(request("POST", "/api/simulations/run", auth(), "https://other.example").getStatus()).isEqualTo(403);
    assertThat(request("POST", "/api/simulations/run", auth(), "http://localhost").getStatus()).isEqualTo(204);
  }
  @Test void healthProbeIsPublicAndHeadersAreSafe() throws Exception {
    var response = request("GET", "/actuator/health", null, null);
    assertThat(response.getStatus()).isEqualTo(204);
    assertThat(response.getHeader("X-Frame-Options")).isEqualTo("DENY");
  }
  @Test void productionFailsClosedWithoutStrongCredentials() {
    assertThatThrownBy(() -> new DemoAccessFilter(true, "judge", "short")).isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(() -> new DemoAccessFilter(true, "", PASSWORD)).isInstanceOf(IllegalStateException.class);
  }
}
