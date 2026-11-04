package com.traceback;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Only project-test routes can reach the separately authenticated PC worker. */
@Component
@Order(20)
public class ProjectProxyFilter extends OncePerRequestFilter {
  private final URI worker;
  private final String authorization;
  static final int MAX_BODY = 21 * 1024 * 1024;
  public ProjectProxyFilter(@Value("${PROJECT_WORKER_URL:}") String url,
      @Value("${WORKER_USERNAME:}") String username, @Value("${WORKER_PASSWORD:}") String password) {
    worker = url.isBlank() ? null : URI.create(url);
    if (worker != null && (!"https".equals(worker.getScheme()) || worker.getHost() == null
        || worker.getUserInfo() != null || worker.getQuery() != null || worker.getFragment() != null
        || !(worker.getPath().isEmpty() || worker.getPath().equals("/"))
        || username.isBlank() || username.contains(":") || password.length() < 16))
      throw new IllegalStateException("Worker requires an HTTPS origin and separate strong credentials.");
    authorization = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
  }
  static boolean allowed(String method, String path) {
    String id = "[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}";
    return method.equals("GET") && (path.equals("/api/projects") || path.equals("/api/projects/runner") || path.matches("/api/projects/" + id))
        || method.equals("POST") && (path.equals("/api/projects") || path.matches("/api/projects/" + id + "/test"));
  }
  @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    String path = req.getRequestURI();
    if (worker == null || !(path.equals("/api/projects") || path.startsWith("/api/projects/"))) {
      chain.doFilter(req,res); return;
    }
    res.setHeader("Cache-Control", "no-store");
    if (!allowed(req.getMethod(), path) || req.getQueryString() != null) { error(res,404,"Unknown project route."); return; }
    if (req.getContentLengthLong() > MAX_BODY) { error(res,413,"Upload exceeds 21 MB request limit."); return; }
    HttpURLConnection upstream = null;
    try {
      // Buffer only bounded incoming bodies; responses stream without retaining source files on Render.
      byte[] body = req.getInputStream().readNBytes(MAX_BODY + 1);
      if (body.length > MAX_BODY) { error(res,413,"Upload exceeds 21 MB request limit."); return; }
      upstream = connection(path);
      upstream.setInstanceFollowRedirects(false);
      upstream.setConnectTimeout(10000); upstream.setReadTimeout(35000);
      upstream.setRequestMethod(req.getMethod());
      upstream.setRequestProperty("Authorization", authorization);
      upstream.setRequestProperty("Accept", "application/json");
      if (req.getMethod().equals("POST")) {
        upstream.setDoOutput(true);
        if (req.getContentType() != null) upstream.setRequestProperty("Content-Type", req.getContentType());
        upstream.setFixedLengthStreamingMode(body.length);
        try (var out = upstream.getOutputStream()) { out.write(body); }
      }
      int status = upstream.getResponseCode();
      if (status == 401 || status == 403 || status >= 300 && status < 400) {
        error(res,503,"Project worker authentication or URL needs attention."); return;
      }
      res.setStatus(status);
      res.setContentType(upstream.getContentType() == null ? "application/json" : upstream.getContentType());
      var stream = status >= 400 ? upstream.getErrorStream() : upstream.getInputStream();
      if (stream != null) try (stream) { stream.transferTo(res.getOutputStream()); }
    } catch (IOException unavailable) {
      if (!res.isCommitted()) { res.resetBuffer(); error(res,503,"Project worker is offline. Keep the PC, Docker, worker and tunnel running."); }
    } finally { if (upstream != null) upstream.disconnect(); }
  }
  HttpURLConnection connection(String path) throws IOException { return (HttpURLConnection)worker.resolve(path).toURL().openConnection(); }
  private static void error(HttpServletResponse res, int status, String message) throws IOException {
    res.setStatus(status); res.setContentType("application/json");
    res.getWriter().write("{\"message\":\"" + message + "\"}");
  }
}
