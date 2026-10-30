# Architecture

## Boundaries

The React application calls a same-origin typed `/api` client. Vite proxies development requests to the loopback-only Spring service. Routes are hash-based and preserve run IDs for reopening and browser refresh. Production assets require a reverse proxy for `/api`.

The Java modular monolith separates the deterministic `Simulator`, persistence-oriented `RunService`, external-analysis `InvestigationService` / `InvestigationProvider`, HTTP `MetaProvider`, DTO records in `Models`, and REST/controller error handling. JPA entities never cross the API boundary.

## Execution

`Simulator.simulate(runId, scenario, baseTime)` has no network, randomness or database side effects. Given identical inputs, every event ID, order ID, sequence, timestamp and outcome is identical. UUIDs are generated only by the orchestration layer; timestamps advance in logical 100ms increments and are not wall-clock performance measurements. Every run has independent collections, correlation ID and idempotency key. Payments are simulated.

Normal payment commits once. Payment failure commits nothing. Duplicate mode deliberately bypasses retry protection and commits twice. Protected retry looks up the previously committed operation by the same key and suppresses a second creation. The key map is scoped to a single controlled run; this is not production distributed idempotency infrastructure.

Verification measures stored orders and distinct logical IDs. Duplicate repair creates a separate protected replay, retains the original run, and stores both metric sets, five named checks, timestamp, explanation, and replay trace. Other scenarios check their stored invariants. A normal/payment run is not described as having had a repair applied. AI cannot decide pass/fail or execute arbitrary code.

## Persistence

- `simulation_run`: UUID, scenario, timestamp, terminal status.
- `trace_event`: child FK to run, ordered sequence, bounded simulator-generated DTO JSON. Event IDs are primary keys; `(run_id, sequence_number)` is unique.
- `simulated_order`: child FK to run, logical ID, idempotency key, status. Logical IDs intentionally are not unique, so the duplicate bug can be recorded.
- `result_artifact`: FK to run, kind (`INVESTIGATION` or `VERIFICATION`), timestamp and validated result JSON. This is the justified storage equivalent of InvestigationResult and VerificationResult entities. Each verification execution is retained; GET returns the latest.

Runs and children are saved transactionally. Artifacts are saved in separate transactions after external calls; database locks are not held during paid inference. Flyway owns schema evolution; Hibernate uses `validate`. H2 file and PostgreSQL profiles share the migration. No destructive startup/schema recreation occurs. Indexes cover run timestamps, order ownership/logical IDs and latest artifact lookup.

## External analysis

Only the fixed verified Meta HTTPS destination accepts the secret. Redirects are disabled. Provider and model are configured outside source; `InvestigationProvider` permits a replacement implementation. Current runtime provider selection supports only `meta`; other values surface unsupported configuration rather than an invented contract.

A maximum 64 events / 24,000 characters of actual trace is sent in one request. The provider uses 5s connect and 30s read timeouts, a 2,000-token completion cap, JSON output mode, a 64KiB response cap, and zero automatic retries. Successful responses must finish normally. Analysis validation enforces text/list bounds, required fields, UUID parsing, and evidence ownership. Refusals, truncation, malformed JSON, foreign evidence and unsuccessful calls are not saved. React renders analysis as escaped text. No model-generated code executes.

Investigations are synchronized in this single-process application and saved output is reused. Multi-instance deduplication, job queues, cancellation and provider history are not implemented. Verification execution is independent and always available without AI.

## Scale and deployment limits

The history list is bounded to 100 rows per request. Dashboard aggregation currently loads persisted runs and their latest artifacts, suitable for a local demonstration; use database aggregates before large datasets. No authentication or multi-user authorization is included. Both services bind to loopback; do not expose this development build to a public network without adding access control, request limits, TLS and deployment review.

## Provider update — 2026-10-30

`AiProviderRouter` is the primary InvestigationProvider bean. It selects `meta` or `nvidia` without automatic fallback; the NVIDIA client sends the saved trace only to its verified fixed NVIDIA HTTPS endpoint. The same size bounds, evidence ownership validation, saved-analysis caching, secret redaction, and independent verification apply. Provider and model are stored with each successful investigation. Successful historical analysis remains cached even if the current provider changes, retaining its original provider/model metadata.
