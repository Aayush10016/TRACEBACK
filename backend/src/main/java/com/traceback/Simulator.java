package com.traceback;

import static com.traceback.Models.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class Simulator {
  public Run simulate(UUID id, Scenario scenario, Instant time) {
    var trace = new ArrayList<Event>();
    var orders = new ArrayList<Order>();
    var committedByKey = new HashMap<String, Order>();
    String logical = "order-" + id, key = "idem-" + id;
    java.util.function.BiConsumer<String, String> emit =
        (type, message) -> {
          int seq = trace.size() + 1;
          String severity =
              type.contains("FAILED") || type.equals("DUPLICATE_ORDER_DETECTED")
                  ? "ERROR"
                  : type.equals("RESPONSE_TIMEOUT") || type.equals("RETRY_STARTED")
                      ? "WARN"
                      : "INFO";
          String component =
              type.startsWith("PAYMENT")
                  ? "payment-gateway"
                  : type.startsWith("ORDER") || type.equals("DUPLICATE_ORDER_DETECTED")
                      ? "order-service"
                      : "orchestrator";
          trace.add(
              new Event(
                  stable(id, "event-" + seq),
                  id,
                  seq,
                  time.plusMillis(seq * 100L),
                  type,
                  component,
                  severity,
                  message,
                  Map.of(
                      "logicalOrderId",
                      logical,
                      "attempt",
                      type.equals("RETRY_STARTED")
                              || trace.stream().anyMatch(e -> e.type().equals("RETRY_STARTED"))
                          ? "2"
                          : "1"),
                  id.toString(),
                  key));
        };
    emit.accept("ORDER_REQUESTED", "Order accepted by the orchestrator");
    emit.accept("PAYMENT_REQUESTED", "Payment authorization requested (simulated)");
    if (scenario == Scenario.PAYMENT_FAILURE) {
      emit.accept("PAYMENT_FAILED", "Payment declined; no order will be created");
      emit.accept("WORKFLOW_FAILED", "Workflow stopped after payment failure");
    } else {
      emit.accept("PAYMENT_SUCCEEDED", "Payment succeeded internally (simulated)");
      Order initial = new Order(stable(id, "order-1"), logical, key, "COMPLETED");
      orders.add(initial);
      committedByKey.put(key, initial);
      emit.accept("ORDER_CREATED", "Committed the first order");
      if (scenario != Scenario.NORMAL_ORDER) {
        emit.accept("RESPONSE_TIMEOUT", "Client did not receive the successful response");
        emit.accept("RETRY_STARTED", "Client retried the same logical operation");
        if (scenario == Scenario.DUPLICATE_ORDER) {
          orders.add(new Order(stable(id, "order-2"), logical, key, "COMPLETED"));
          emit.accept(
              "ORDER_CREATED", "Retry committed a second order without idempotency protection");
          emit.accept(
              "DUPLICATE_ORDER_DETECTED", "Two orders share the same logical order identifier");
          emit.accept("WORKFLOW_FAILED", "Duplicate-order invariant violated");
        } else {
          Order existing = committedByKey.get(key);
          if (existing == null)
            throw new IllegalStateException("Committed idempotency operation is missing");
          emit.accept(
              "IDEMPOTENCY_KEY_REUSED",
              "Existing committed order returned; second creation suppressed");
          emit.accept("WORKFLOW_COMPLETED", "Retry completed with exactly one logical order");
        }
      } else emit.accept("WORKFLOW_COMPLETED", "Order workflow completed");
    }
    return new Run(
        id,
        scenario,
        time,
        scenario == Scenario.DUPLICATE_ORDER || scenario == Scenario.PAYMENT_FAILURE
            ? "FAILED"
            : "COMPLETED",
        List.copyOf(trace),
        List.copyOf(orders));
  }

  static UUID stable(UUID id, String suffix) {
    return UUID.nameUUIDFromBytes((id + ":" + suffix).getBytes(StandardCharsets.UTF_8));
  }

  public Metrics metrics(Run run) {
    int unique = (int) run.orders().stream().map(Order::logicalOrderId).distinct().count();
    return new Metrics(run.orders().size(), unique, run.orders().size() - unique, run.status());
  }

  public Verification verify(Run before) {
    boolean repair = before.scenario() == Scenario.DUPLICATE_ORDER;
    Run after =
        repair
            ? simulate(UUID.randomUUID(), Scenario.TIMEOUT_WITH_IDEMPOTENCY, Instant.now())
            : before;
    var b = metrics(before);
    var a = metrics(after);
    int expected = before.scenario() == Scenario.PAYMENT_FAILURE ? 0 : 1;
    String status = before.scenario() == Scenario.PAYMENT_FAILURE ? "FAILED" : "COMPLETED";
    var checks =
        List.of(
            check("Orders created", expected, a.ordersCreated()),
            check("Unique logical orders", expected, a.uniqueLogicalOrders()),
            check("Duplicate count", 0, a.duplicateCount()),
            check("Workflow status", status, a.workflowStatus()),
            check(
                "Idempotency recognized",
                repair || before.scenario() == Scenario.TIMEOUT_WITH_IDEMPOTENCY,
                after.events().stream().anyMatch(e -> e.type().equals("IDEMPOTENCY_KEY_REUSED"))));
    return new Verification(
        UUID.randomUUID(),
        before.id(),
        Instant.now(),
        checks.stream().allMatch(Check::passed) ? "PASSED" : "FAILED",
        repair,
        repair
            ? "Idempotency protection applied to a separate controlled replay; original trace"
                + " preserved."
            : "Scenario invariants checked against the stored run.",
        b,
        a,
        checks,
        repair ? after : null);
  }

  private Check check(String name, Object expected, Object actual) {
    boolean pass = expected.equals(actual);
    return new Check(
        name,
        expected.toString(),
        actual.toString(),
        pass,
        pass ? null : "Expected " + expected + " but observed " + actual);
  }
}
