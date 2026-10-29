package com.traceback;

import static com.traceback.Models.*;
import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.*;

class SimulatorTest {
  private final Simulator simulator = new Simulator();

  private Run run(Scenario s) {
    return simulator.simulate(UUID.randomUUID(), s, Instant.parse("2026-01-01T00:00:00Z"));
  }

  @Test
  void normalCreatesOne() {
    var r = run(Scenario.NORMAL_ORDER);
    assertThat(r.orders()).hasSize(1);
    assertThat(r.status()).isEqualTo("COMPLETED");
  }

  @Test
  void duplicateCreatesTwoForOneLogicalOrder() {
    var r = run(Scenario.DUPLICATE_ORDER);
    assertThat(r.orders()).hasSize(2);
    assertThat(simulator.metrics(r).uniqueLogicalOrders()).isEqualTo(1);
    assertThat(simulator.metrics(r).duplicateCount()).isEqualTo(1);
    assertThat(r.status()).isEqualTo("FAILED");
  }

  @Test
  void failedPaymentCreatesNothing() {
    assertThat(run(Scenario.PAYMENT_FAILURE).orders()).isEmpty();
  }

  @Test
  void idempotencySuppressesDuplicate() {
    var r = run(Scenario.TIMEOUT_WITH_IDEMPOTENCY);
    assertThat(r.orders()).hasSize(1);
    assertThat(r.events()).extracting(Event::type).contains("IDEMPOTENCY_KEY_REUSED");
  }

  @Test
  void eventsHaveCorrectOrdering() {
    var r = run(Scenario.DUPLICATE_ORDER);
    assertThat(r.events())
        .extracting(Event::type)
        .containsExactly(
            "ORDER_REQUESTED",
            "PAYMENT_REQUESTED",
            "PAYMENT_SUCCEEDED",
            "ORDER_CREATED",
            "RESPONSE_TIMEOUT",
            "RETRY_STARTED",
            "ORDER_CREATED",
            "DUPLICATE_ORDER_DETECTED",
            "WORKFLOW_FAILED");
    for (int i = 0; i < r.events().size(); i++) {
      assertThat(r.events().get(i).sequence()).isEqualTo(i + 1);
      assertThat(r.events().get(i).timestamp()).isEqualTo(r.createdAt().plusMillis((i + 1) * 100L));
    }
  }

  @Test
  void repeatedInputsProduceIdenticalOutputs() {
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    assertThat(simulator.simulate(id, Scenario.DUPLICATE_ORDER, now))
        .isEqualTo(simulator.simulate(id, Scenario.DUPLICATE_ORDER, now));
  }

  @Test
  void historiesAreIsolated() {
    var a = run(Scenario.NORMAL_ORDER);
    var b = run(Scenario.NORMAL_ORDER);
    assertThat(a.events())
        .extracting(Event::id)
        .doesNotContainAnyElementsOf(b.events().stream().map(Event::id).toList());
    assertThat(a.events()).allMatch(e -> e.runId().equals(a.id()));
  }

  @Test
  void verificationUsesRealBeforeAndRepairedAfter() {
    var r = run(Scenario.DUPLICATE_ORDER);
    var v = simulator.verify(r);
    assertThat(v.before().ordersCreated()).isEqualTo(2);
    assertThat(v.after().ordersCreated()).isEqualTo(1);
    assertThat(v.correctionApplied()).isTrue();
    assertThat(v.checks()).allMatch(Check::passed);
    assertThat(r.orders()).hasSize(2);
    assertThat(v.repairedRun().events()).extracting(Event::type).contains("IDEMPOTENCY_KEY_REUSED");
  }

  @Test
  void verificationDetectsInvalidStoredOutcomes() {
    var r = run(Scenario.NORMAL_ORDER);
    var bad =
        new Run(
            r.id(),
            r.scenario(),
            r.createdAt(),
            "COMPLETED",
            r.events(),
            List.of(r.orders().get(0), r.orders().get(0)));
    assertThat(simulator.verify(bad).status()).isEqualTo("FAILED");
  }

  @Test
  void retryMetadataUsesSecondAttempt() {
    var r = run(Scenario.DUPLICATE_ORDER);
    assertThat(r.events().get(6).metadata().get("attempt")).isEqualTo("2");
  }

  @Test
  void everyScenarioHasPassingInvariants() {
    for (var s : Scenario.values()) {
      assertThat(simulator.verify(run(s)).status()).isEqualTo("PASSED");
    }
  }
}
