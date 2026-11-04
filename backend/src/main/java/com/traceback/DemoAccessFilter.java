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
  @Value("${DEMO_PUBLIC_ORIGIN:}")
  private String publicOrigin = "";
  @Value("${traceback.demo.public-workspace:false}")
  private boolean publicWorkspace;
  private final byte[] expected;
  private final RequestBudget simulations = new RequestBudget(30, 60_000);
  private final RequestBudget investigations = new RequestBudget(20, 86_400_000);
  private final RequestBudget investigationBurst = new RequestBudget(1, 60_000);
  private static final String ID = "[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}";

  public DemoAccessFilter(
      @Value("${traceback.demo.access-enabled:false}") boolean enabled,
      @Value("${DEMO_USERNAME:}") String username,
      @Value("${DEMO_PASSWORD:}") String password) {
    if (enabled && (username.isBlank() || username.contains(":") || password.length() < 16))
      throw new IllegalStateException("Production demo requires DEMO_USERNAME and a DEMO_PASSWORD of at least 16 characters.");
    this.enabled = enabled;
    this.expected = (username + ":" + password).getBytes(StandardCharsets.UTF_8);
  }

  private void handlePublicRequest(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    String path = req.getRequestURI();
    String method = req.getMethod();
    if (method.equals("GET") && (path.equals("/api/health") || path.equals("/api/dashboard")
        || path.equals("/api/simulations") || path.equals("/api/simulations/scenarios")
        || path.matches("/api/simulations/" + ID + "(?:/events|/orders)?")
        || path.matches("/api/(?:investigations|verification)/" + ID))) {
      chain.doFilter(req, res);
      return;
    }
    if (!method.equals("POST") || !(path.equals("/api/simulations/run")
        || path.matches("/api/(?:investigations|verification)/" + ID))) {
      reply(res, 403, "This action is unavailable in the public workspace.");
      return;
    }
    if ("cross-site".equals(req.getHeader("Sec-Fetch-Site"))
        || publicOrigin.isBlank() || !publicOrigin.equals(req.getHeader("Origin"))) {
      reply(res, 403, "Start this action from the public TRACEBACK site.");
      return;
    }
    if (path.startsWith("/api/investigations/")) {
      if (!investigationBurst.take() || !investigations.take()) {
        reply(res, 429, "The shared AI demo limit has been reached. Try later or view the saved analysis on the homepage.");
        return;
      }
    } else if (!simulations.take()) {
      reply(res, 429, "The shared simulation limit has been reached. Try again in a minute.");
      return;
    }
    chain.doFilter(req, res);
  }

  private static void reply(HttpServletResponse response, int status, String message) throws IOException {
    response.setStatus(status);
    response.setContentType("application/json");
    response.getWriter().write("{\"message\":\"" + message + "\"}");
  }

  private static final class RequestBudget {
    private final int limit;
    private final long interval;
    private long start = System.currentTimeMillis();
    private int used;

    RequestBudget(int limit, long interval) {
      this.limit = limit;
      this.interval = interval;
    }

    synchronized boolean take() {
      long now = System.currentTimeMillis();
      if (now - start >= interval) {
        start = now;
        used = 0;
      }
      if (used >= limit) return false;
      used++;
      return true;
    }
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
    if (publicWorkspace) {
      handlePublicRequest(req, res, chain);
      return;
    }
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
      if ("cross-site".equals(req.getHeader("Sec-Fetch-Site")) || (origin != null && !origin.equalsIgnoreCase(expectedOrigin) && !origin.equals(publicOrigin))) {
        res.setStatus(403);
        res.setContentType("application/json");
        res.getWriter().write("{\"message\":\"Cross-origin changes are not allowed.\"}");
        return;
      }
    }
    chain.doFilter(req, res);
  }
}
