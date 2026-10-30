package com.traceback;

import static com.traceback.Models.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Validated
public class ApiController {
  private final RunService runs;
  private final InvestigationService investigations;
  private final AiProviderRouter ai;
  private final JdbcTemplate jdbc;

  public ApiController(RunService r, InvestigationService i, AiProviderRouter m, JdbcTemplate j) {
    runs = r;
    investigations = i;
    ai = m;
    jdbc = j;
  }

  public record RunRequest(@NotNull Scenario scenario) {}

  @GetMapping("/health")
  public Map<String, Object> health() {
    jdbc.queryForObject("SELECT 1", Integer.class);
    var status = ai.status();
    return Map.of("backend", "UP", "database", "UP", "ai", status, "meta", status);
  }

  @GetMapping("/dashboard")
  public Map<String, Object> dashboard() {
    return runs.stats();
  }

  @GetMapping("/simulations/scenarios")
  public Scenario[] scenarios() {
    return Scenario.values();
  }

  @PostMapping("/simulations/run")
  public ResponseEntity<Run> create(@Valid @RequestBody RunRequest request) {
    Run run = runs.create(request.scenario());
    return ResponseEntity.created(java.net.URI.create("/api/simulations/" + run.id())).body(run);
  }

  @GetMapping("/simulations")
  public Models.Page<Summary> list(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return runs.list(page, size);
  }

  @GetMapping("/simulations/{id}")
  public Run get(@PathVariable UUID id) {
    return runs.get(id);
  }

  @GetMapping("/simulations/{id}/events")
  public List<Event> events(@PathVariable UUID id) {
    return runs.get(id).events();
  }

  @GetMapping("/simulations/{id}/orders")
  public List<Order> orders(@PathVariable UUID id) {
    return runs.get(id).orders();
  }

  @PostMapping("/investigations/{id}")
  public Investigation investigate(@PathVariable UUID id) {
    return investigations.investigate(id);
  }

  @GetMapping("/investigations/{id}")
  public ResponseEntity<Investigation> investigation(@PathVariable UUID id) {
    return runs.latest(id, "INVESTIGATION", Investigation.class)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.noContent().build());
  }

  @PostMapping("/verification/{id}")
  public Verification verify(@PathVariable UUID id) {
    return runs.verify(id);
  }

  @GetMapping("/verification/{id}")
  public ResponseEntity<Verification> verification(@PathVariable UUID id) {
    return runs.latest(id, "VERIFICATION", Verification.class)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.noContent().build());
  }
}
