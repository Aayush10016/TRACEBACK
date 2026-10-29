package com.traceback;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

public final class Entities {
  private Entities() {}

  @Entity
  @Table(name = "simulation_run")
  public static class RunEntity {
    @Id public UUID id;

    @Column(nullable = false)
    public String scenario;

    @Column(nullable = false)
    public Instant createdAt;

    @Column(nullable = false)
    public String status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "run_id", nullable = false)
    @OrderBy("sequence ASC")
    public List<EventEntity> events = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "run_id", nullable = false)
    public List<OrderEntity> orders = new ArrayList<>();
  }

  @Entity
  @Table(name = "trace_event")
  public static class EventEntity {
    @Id public UUID id;

    @Column(name = "sequence_number", nullable = false)
    public int sequence;

    @Column(nullable = false, columnDefinition = "text")
    public String payload;
  }

  @Entity
  @Table(name = "simulated_order")
  public static class OrderEntity {
    @Id public UUID id;

    @Column(nullable = false)
    public String logicalOrderId;

    @Column(nullable = false)
    public String idempotencyKey;

    @Column(nullable = false)
    public String status;
  }

  @Entity
  @Table(name = "result_artifact")
  public static class Artifact {
    @Id public UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    public RunEntity run;

    @Column(nullable = false)
    public String kind;

    @Column(nullable = false)
    public Instant createdAt;

    @Column(nullable = false, columnDefinition = "text")
    public String payload;
  }
}
