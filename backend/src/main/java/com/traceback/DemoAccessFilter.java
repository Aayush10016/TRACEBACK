package com.traceback;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Shared, password-protected hackathon demo; not a multi-user account system. */
@Component
@Order(10)
public class DemoAccessFilter extends OncePerRequestFilter {
  private final boolean enabled;
  private final byte[] expected;

  public DemoAccessFilter(
      @Value("${traceback.demo.access-enabled:false}") boolean enabled,
      @Value("${DEMO_USERNAME:}") String username,
      @Value("${DEMO_PASSWORD:}") String password) {
    if (enabled && (username.isBlank() || username.contains(":") || password.length() < 16))
      throw new IllegalStateException("Production demo requires DEMO_USERNAME and a DEMO_PASSWORD of at least 16 characters.");
    this.enabled = enabled;
    this.expected = (username + ":" + password).getBytes(StandardCharsets.UTF_8);
  }

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    res.setHeader("X-Content-Type-Options", "nosniff");
    res.setHeader("X-Frame-Options", "DENY");
    res.setHeader("Referrer-Policy", "same-origin");
    if (!enabled || req.getRequestURI().equals("/actuator/health")) {
      chain.doFilter(req, res);
      return;
    }
    res.setHeader("Cache-Control", "no-store");
    String auth = req.getHeader("Authorization");
    byte[] supplied = new byte[0];
    try {
      if (auth != null && auth.regionMatches(true, 0, "Basic ", 0, 6))
        supplied = Base64.getDecoder().decode(auth.substring(6));
    } catch (IllegalArgumentException ignored) { /* Invalid credentials receive the same challenge. */ }
    if (!MessageDigest.isEqual(expected, supplied)) {
      res.setHeader("WWW-Authenticate", "Basic realm=\"TRACEBACK demo\", charset=\"UTF-8\"");
      res.setStatus(401);
      res.setContentType("application/json");
      res.getWriter().write("{\"message\":\"Demo sign-in required.\"}");
      return;
    }
    if (!req.getMethod().equals("GET") && !req.getMethod().equals("HEAD")) {
      String origin = req.getHeader("Origin");
      String port = req.getServerPort() == 80 || req.getServerPort() == 443 ? "" : ":" + req.getServerPort();
      String expectedOrigin = req.getScheme() + "://" + req.getServerName() + port;
      if ("cross-site".equals(req.getHeader("Sec-Fetch-Site")) || (origin != null && !origin.equalsIgnoreCase(expectedOrigin))) {
        res.setStatus(403);
        res.setContentType("application/json");
        res.getWriter().write("{\"message\":\"Cross-origin changes are not allowed.\"}");
        return;
      }
    }
    chain.doFilter(req, res);
  }
}
