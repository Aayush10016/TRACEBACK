package com.traceback;

import java.time.Instant;
import java.util.*;

public final class Models {
  private Models() {}

  public enum Scenario {
    NORMAL_ORDER,
    DUPLICATE_ORDER,
    PAYMENT_FAILURE,
    TIMEOUT_WITH_IDEMPOTENCY
  }

  public record Event(
      UUID id,
      UUID runId,
      int sequence,
      Instant timestamp,
      String type,
      String component,
      String severity,
      String message,
      Map<String, String> metadata,
      String correlationId,
      String idempotencyKey) {}

  public record Order(UUID id, String logicalOrderId, String idempotencyKey, String status) {}

  public record Run(
      UUID id,
      Scenario scenario,
      Instant createdAt,
      String status,
      List<Event> events,
      List<Order> orders) {}

  public record Check(
      String testName, String expected, String actual, boolean passed, String failureExplanation) {}

  public record Metrics(
      int ordersCreated, int uniqueLogicalOrders, int duplicateCount, String workflowStatus) {}

  public record Verification(
      UUID id,
      UUID runId,
      Instant executedAt,
      String status,
      boolean correctionApplied,
      String correction,
      Metrics before,
      Metrics after,
      List<Check> checks,
      Run repairedRun) {}

  public record Analysis(
      String rootCauseHypothesis,
      String causalExplanation,
      List<UUID> evidenceEventIds,
      String directEvidence,
      String inference,
      List<String> alternativeExplanations,
      List<String> missingInformation,
      String uncertainty,
      String recommendedCorrection,
      String verificationPlan,
      List<String> risksAndAssumptions) {}

  public record Investigation(
      UUID id,
      UUID runId,
      Instant createdAt,
      String status,
      String provider,
      String model,
      Analysis analysis) {}

  public record Summary(
      UUID id,
      Scenario scenario,
      Instant createdAt,
      String status,
      int eventCount,
      int orderCount,
      int duplicateCount,
      String investigationStatus,
      String verificationStatus) {}

  public record Page<T>(List<T> content, long totalElements, int page, int size, boolean last) {}
}
