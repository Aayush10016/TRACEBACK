package com.traceback;
import static org.assertj.core.api.Assertions.*;
import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;

class ProjectProxyFilterTest {
  @Test void forwardsMultipartAndOnlyWorkerCredentials() throws Exception {
    var seen = new AtomicReference<String>();
    var server = HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
    server.createContext("/api/projects", x -> {
      seen.set(x.getRequestHeaders().getFirst("Authorization") + "|" + x.getRequestHeaders().getFirst("Origin") + "|" + x.getRequestHeaders().getFirst("Content-Type") + "|" + new String(x.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));
      byte[] response = "{\"status\":\"READY\"}".getBytes(StandardCharsets.UTF_8);
      x.getResponseHeaders().set("Content-Type","application/json"); x.sendResponseHeaders(200,response.length);
      try (var out=x.getResponseBody()) {out.write(response);}
    });
    server.start();
    try {
      var filter = new ProjectProxyFilter("https://worker.example","worker","test-worker-password-long") {
        @Override HttpURLConnection connection(String path) throws IOException {
          return (HttpURLConnection)new URL("http://127.0.0.1:"+server.getAddress().getPort()+path).openConnection();
        }
      };
      var req = new MockHttpServletRequest("POST","/api/projects");
      req.setContentType("multipart/form-data; boundary=sample");
      req.addHeader("Origin","https://demo.vercel.app"); req.addHeader("Authorization","Basic judge-secret");
      req.setContent("sample ZIP bytes".getBytes(StandardCharsets.UTF_8));
      var res = new MockHttpServletResponse();
      filter.doFilter(req,res,(a,b)->{throw new AssertionError("Must proxy");});
      assertThat(res.getStatus()).isEqualTo(200);
      assertThat(res.getContentAsString()).contains("READY");
      assertThat(seen.get()).endsWith("|null|multipart/form-data; boundary=sample|sample ZIP bytes").doesNotContain("judge-secret");
      assertThat(res.getHeader("Cache-Control")).isEqualTo("no-store");
    } finally {server.stop(0);}
  }
  @Test void offlineWorkerReturnsHonest503WithoutDetails() throws Exception {
    var filter=new ProjectProxyFilter("https://worker.example","worker","test-worker-password-long"){
      @Override HttpURLConnection connection(String path) throws IOException {throw new IOException("secret diagnostic");}
    };
    var res=new MockHttpServletResponse();
    filter.doFilter(new MockHttpServletRequest("GET","/api/projects/runner"),res,(a,b)->{});
    assertThat(res.getStatus()).isEqualTo(503);
    assertThat(res.getContentAsString()).contains("offline").doesNotContain("secret diagnostic");
  }
  @Test void rejectsUnsafeRoutesAndConfiguration() {
    assertThat(ProjectProxyFilter.allowed("POST","/api/projects/../investigations")).isFalse();
    assertThat(ProjectProxyFilter.allowed("DELETE","/api/projects")).isFalse();
    assertThat(ProjectProxyFilter.allowed("POST","/api/projects/6abeb3f7-1266-4556-9690-409e364c3b9d/test")).isTrue();
    assertThatThrownBy(()->new ProjectProxyFilter("http://worker.example","worker","test-worker-password-long")).isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(()->new ProjectProxyFilter("https://worker.example/path","worker","test-worker-password-long")).isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(()->new ProjectProxyFilter("https://worker.example","worker","short")).isInstanceOf(IllegalStateException.class);
  }
  @Test void rejectsLargeRequestsAndRedirects() throws Exception {
    var filter=new ProjectProxyFilter("https://worker.example","worker","test-worker-password-long");
    var req=new MockHttpServletRequest("POST","/api/projects");
    req.setContent(new byte[ProjectProxyFilter.MAX_BODY+1]);
    var res=new MockHttpServletResponse();filter.doFilter(req,res,(a,b)->{});
    assertThat(res.getStatus()).isEqualTo(413);
    var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
    server.createContext("/api/projects",x->{x.getResponseHeaders().set("Location","https://elsewhere.example");x.sendResponseHeaders(302,-1);x.close();});
    server.start();
    try {
      var redirectFilter=new ProjectProxyFilter("https://worker.example","worker","test-worker-password-long"){
        @Override HttpURLConnection connection(String path) throws IOException {return (HttpURLConnection)new URL("http://127.0.0.1:"+server.getAddress().getPort()+path).openConnection();}
      };
      var response=new MockHttpServletResponse();redirectFilter.doFilter(new MockHttpServletRequest("GET","/api/projects"),response,(a,b)->{});
      assertThat(response.getStatus()).isEqualTo(503);
      assertThat(response.getHeader("Location")).isNull();
    } finally {server.stop(0);}
  }
}
