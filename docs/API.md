# REST API

Base: `http://127.0.0.1:8080/api`. JSON responses use ISO-8601 timestamps and UUID IDs. JPA entities are private persistence types; API payloads use DTO records.

| Method | Path | Result |
|---|---|---|
| GET | /health | Backend/database health and safe Meta configuration state |
| GET | /dashboard | Actual persisted aggregates; pass rate -1 when no verified run exists |
| GET | /simulations/scenarios | Four supported scenario identifiers |
| POST | /simulations/run | 201, new run with stored events and orders; Location header |
| GET | /simulations?page=0&size=20 | Bounded history page (size 1–100, page >= 0) |
| GET | /simulations/{runId} | Complete stored original run |
| GET | /simulations/{runId}/events | Ordered actual trace events |
| GET | /simulations/{runId}/orders | Actual recorded orders |
| POST | /investigations/{runId} | Explicit model call, or reused saved validated analysis |
| GET | /investigations/{runId} | Latest analysis; 204 if not generated |
| POST | /verification/{runId} | Executes checks and persists a new result |
| GET | /verification/{runId} | Latest verification; 204 if never run |

## Examples

Create a controlled failure:

```http
POST /api/simulations/run
Content-Type: application/json

{"scenario":"DUPLICATE_ORDER"}
```

The returned run contains `id`, `scenario`, `createdAt`, `status`, `events`, and `orders`. This scenario returns `FAILED`, nine events, and two completed orders sharing a logical order ID. IDs and timestamps are generated; no placeholder UUID is a valid existing resource.

Each event contains `id`, `runId`, `sequence`, `timestamp`, `type`, `component`, `severity`, `message`, structured `metadata`, `correlationId`, and `idempotencyKey`. Sequence is stable and unique within a run; timestamps represent logical simulation time. Retry metadata uses attempt 2.

Pagination shape:

```json
{"content":[],"totalElements":0,"page":0,"size":20,"last":true}
```

Verification includes an ID, run ID, execution timestamp, `PASSED`/`FAILED`, `correctionApplied`, correction explanation, before/after metrics, named checks with expected/actual/pass/failureExplanation, and a protected replay DTO when applicable. For a duplicate run, expected comparison is 2 orders / 1 logical / 1 duplicate before and 1 / 1 / 0 after. Passing is calculated from Java checks. For payment failure, zero orders and failed workflow are the expected invariants.

Errors have `timestamp`, numeric `status`, stable `code`, and user-safe `message`. Invalid JSON/scenario/UUID/pagination returns 400. Missing runs/endpoints return 404; invalid methods return 405; wrong content type returns 415. Missing Meta key returns 503 `META_NOT_CONFIGURED`; invalid or inaccessible credentials/models return 502 with a safe `META_HTTP_*` code; rate limiting returns 429; timeout returns 504; malformed/foreign evidence returns 502. No provider body, authorization header or stack trace is returned. Failed investigations remain absent and can be retried explicitly.

## Verified Meta contract (2026-10-29)

Official sources:

- [Meta quickstart](https://dev.meta.ai/docs/cookbook/quickstart-chat-completions)
- [Create chat completion](https://dev.meta.ai/docs/api-reference/chat-completions/create-chat-completion)
- [Request/response schemas](https://dev.meta.ai/docs/api-reference/chat-completions/schemas)
- [Structured output](https://dev.meta.ai/docs/structured-output)

The implemented request is `POST https://api.meta.ai/v1/chat/completions`, `Authorization: Bearer <backend key>`, JSON content type. Configuration maps `META_API_KEY` to that bearer value (Meta's example uses the name `MODEL_API_KEY`; environment naming is application-owned). Default model `muse-spark-1.3` is listed in the current official quickstart. Request includes model, developer/user messages, `max_completion_tokens: 2000`, and `response_format: {"type":"json_object"}`. Output is read from `choices[0].message.content`; `finish_reason` must be `stop`.

Analysis requires `rootCauseHypothesis`, `causalExplanation`, `evidenceEventIds`, `directEvidence`, `inference`, `alternativeExplanations`, `missingInformation`, `uncertainty`, `recommendedCorrection`, `verificationPlan`, and `risksAndAssumptions`. Evidence UUIDs must belong to the same stored run. JSON mode constrains syntax; Java validates the semantic contract. No real authenticated request was executed because credentials were not available. Documentation verification is separate from account/model access verification.

Only the documented endpoint is accepted. The configured model can change within supported Meta access; use another provider by implementing the abstraction and verifying its official contract first. Do not point an API key at an unverified URL.
