# TRACEBACK — implementation report

Date: 2026-10-29, Asia/Calcutta. Project: **D:\TRACEBACK**. The uploaded complete specification is preserved as `MASTER_SPECIFICATION.md`.

## 1. Executive summary

Built a working local full-stack controlled incident investigation application: deterministic order simulation, relational persistence, REST APIs, five connected dark UI screens, event inspection, protected replay, executable verification and persistent history. The Meta HTTP integration follows the verified official contract and rejects unsupported/invalid analysis. Authenticated AI access is **not verified**, because no key was available. No fake AI output, seeded dashboard metrics, real payments or external code edits are used.

## 2. Environment

Windows 11; Oracle Java 17.0.12; installed Maven 3.9.11; project-local Maven **3.9.16** used for actual builds; Node 24.18.0; npm 11.16.0; Git 2.47.1.windows.1; PostgreSQL **17.6** binaries/service; Docker installed but its engine stopped. Spring Boot **3.5.15** is pinned. Frontend lockfile resolves React 19 / Vite 7.3.7 / TypeScript 5.9.3.

D: existed; D:\TRACEBACK did not. A new directory/repository was created there; no existing source, commits or remotes were replaced. Writing this requested drive required execution approval; all application source/docs/scripts remain on D:. The cached uploaded attachment was recovered through the referenced conversation because `/mnt/data/Pasted text(4).txt` was not a Windows path.

Existing PostgreSQL accepted connections but required an unavailable password. It was left untouched. A separate project-local cluster with generated credentials and loopback-only port 55433 enabled PostgreSQL QA. No credentials appear in this report. Tools, caches, QA data and generated password are Git-ignored under `.tools`.

The installed Java's Windows socket startup failed under the long temporary path. A short `.tools/tmp` directory fixed it; the startup script sets this only for its process. No global tool configuration changed.

## 3. Architecture

React typed client → Vite same-origin `/api` proxy → Spring REST controllers → run/simulation/investigation services → JPA/Flyway. Pure Java simulator emits deterministic logical-time events. PostgreSQL and persistent H2 profiles share migrations. Analysis uses the replaceable InvestigationProvider abstraction and a verified Meta implementation; verification is independent Java logic. See `docs/ARCHITECTURE.md`.

## 4. Feature inventory

Complete and verified: four scenarios; ordered isolated traces; run/order/event persistence; validation/errors/pagination; dashboard aggregates; event metadata inspection; deterministic replay/checks; persisted result reopening; five UI screens; startup/test scripts; backend/frontend checks; real HTTP smoke checks on both databases.

Implemented with partial verification: Meta transport/configuration/error mapping, bounded JSON requests, evidence ownership validation, persisted analysis and evidence links. Automated validation/caching tests use mocks. Real provider access, authentic recommendations and live evidence-link interaction after a real model response remain unverified.

Not implemented: generic external-application debugging, arbitrary code patching, production repository modification, multi-user authentication, distributed jobs or public deployment. These are not presented as operational.

## 5. File inventory

- `backend/pom.xml`: Java 17 / Boot 3.5.15 dependencies and Maven build.
- `backend/src/main/java/com/traceback/Models.java`: public DTO contracts.
- `Simulator.java`: four scenario flows, per-run idempotency lookup, metrics and executable verification.
- `Entities.java`, `RunRepository.java`, `ArtifactRepository.java`, `RunService.java`: transactions, stored traces, orders, results and aggregates.
- `InvestigationProvider.java`, `MetaProvider.java`, `InvestigationService.java`: transport abstraction, bounded official API call, validation/caching.
- `ApiController.java`, `ApiErrors.java`: endpoints and safe error responses.
- `backend/src/main/resources/db/migration/V1__initial_schema.sql`: portable relational schema.
- `application*.properties`: local/PostgreSQL and safe environment-driven AI configuration.
- `backend/src/test/.../SimulatorTest.java`, `ApiTest.java`: 22 automated tests.
- `frontend/src/App.tsx`, `api.ts`, `styles.css`: five screens, typed API and shared visual system; `App.test.tsx`: four interaction tests.
- `scripts/*.ps1`, `.env.example`, `.gitignore`, `docker-compose.yml`: reproducible local setup with ignored secrets/data.
- `README.md`, `docs/*.md`: setup, architecture, APIs, testing, design and remaining work.
- `docs/*-smoke-results.json`, `docs/verification-ui.jpg`: real integration/visual evidence. Logs/target/build artifacts are ignored.

## 6. API summary

Health, dashboard, scenarios, run creation/list/detail/events/orders, investigation POST/GET, verification POST/GET are implemented. JSON scenario validation; UUID/path handling; page >= 0 and size 1–100; 201 creation with Location; 204 for a result not yet created; 400 validation; 404 unknown resource; 405/415 invalid method/media; safe provider errors. Examples and complete contracts: `docs/API.md`.

## 7. Database

Five conceptual models use four table types: simulation_run, trace_event, simulated_order, result_artifact (investigation and verification kinds). Child FKs, UUID primary keys, unique run/event sequence, enum/check constraints and ownership/latest-result indexes are in Flyway V1. Duplicate logical-order IDs intentionally remain recordable. DTO JSON preserves structured event/result details. Each verification execution persists its ID, time, before/after, checks and replay; GET reopens the latest. JPA validation succeeded on H2 and PostgreSQL.

## 8. Meta API integration

Real HTTP client implemented; **real authenticated integration not verified**. Sources checked: [official quickstart](https://dev.meta.ai/docs/cookbook/quickstart-chat-completions), [endpoint](https://dev.meta.ai/docs/api-reference/chat-completions/create-chat-completion), [schemas](https://dev.meta.ai/docs/api-reference/chat-completions/schemas), [structured output](https://dev.meta.ai/docs/structured-output).

Contract: POST `https://api.meta.ai/v1/chat/completions`, bearer authorization, JSON body with model/messages, 2,000-token cap and JSON output mode; read choices[0].message.content, require normal completion. Default documented model: muse-spark-1.3. Backend variables: META_API_KEY, META_MODEL, META_PROVIDER, META_ENDPOINT. Only the verified endpoint and meta provider are accepted; replacement provider implementations require their own verified contract.

No key was configured. A live missing-key request returned 503 as expected. No real model credits were consumed. Analysis JSON is validated for required fields, text/list bounds and event IDs belonging to that run. No fake successful analysis is substituted.

## 9. Test report

| Command/check | Actual result |
|---|---|
| Project-local Maven 3.9.16 `clean verify` | PASS: 22 tests, 0 failures/errors/skips; packaged JAR |
| `npm run test` | PASS: 4 tests |
| `npm run lint` | PASS |
| `npm run build` | PASS: TypeScript + Vite production assets |
| `scripts/smoke-test.ps1` against H2 :8080 | PASS: all four scenario counts, stored events, verification |
| `scripts/smoke-test.ps1 -BaseUrl http://127.0.0.1:8081` against PostgreSQL | PASS: all four scenarios; Flyway and schema validation succeeded |
| Reopen pre-restart H2 run/verification | PASS: original stored result remains PASSED |
| POST investigation without credentials | PASS: HTTP 503 configuration error |
| Live browser workflow and representative desktop/mobile screenshots | Performed; details below |
| Real authenticated Meta call | NOT VERIFIED — credentials unavailable |
| Docker Compose live startup | NOT VERIFIED — Docker engine stopped |

Exact Maven invocation from backend: `..\.tools\apache-maven-3.9.16\bin\mvn.cmd '-Dmaven.repo.local=D:\TRACEBACK\.tools\m2' --no-transfer-progress clean verify`. `scripts/run-tests.ps1` runs that plus frontend checks. Locally retained logs: docs/validation-final.log and docs/frontend-{test,lint,build}-final.log. Individual Surefire reports are under backend/target/surefire-reports. Actual run IDs/counts: docs/h2-smoke-results.json and docs/postgres-smoke-results.json.

Failures resolved: PowerShell split a Maven option (quoted it); an interface mock replaced a concrete bean (mocked concrete provider); Java temporary socket path too long (short project-local TEMP); Windows running-JAR lock prevented clean (stopped preview backend before rebuilding); initdb option parsing (passed one complete option); Vite restart during formatting interrupted the preview (restarted and opened a fresh tab). No unresolved compilation/test failure is known. Details: docs/TESTING.md.

## 10. Demonstration guide

Run scripts/start-backend.ps1 and scripts/start-frontend.ps1 in two terminals; open 127.0.0.1:5173. Start Duplicate order, inspect actual events and metadata, explicitly investigate if a backend key is configured, select returned evidence, then run verification. Compare 2 orders / 1 logical / 1 duplicate with protected replay 1 / 1 / 0. Inspect five checks and replay events; reopen history. Repeat normal, payment failure and protected timeout. Full commands and key-switching instructions are in README.md. For accurate retry metadata, create a fresh run: the first immutable QA trace predates the metadata correction.

## 11. UI quality

Completed Overview, Investigation, Verification, History and System Status as a coherent dark product. Graphite surfaces, restrained blue/cyan accents, status text/icons, Inter/system fallbacks, monospace technical details, reusable controls, keyboard focus, dialog focus handling, empty/loading/error states and reduced-motion support. Sidebar adapts to horizontal mobile navigation; tables stay in scrollable containers.

Figma authenticated and used for an initial editable visual-direction composition: https://www.figma.com/design/K7qGxbod6fHYBYC8OrHovF. This is not claimed as a finished component library. Actual UI was visually inspected in the in-app browser, including all five screens, laptop layout and 390×844 overview/status. Real simulation launch, event selection, expanded metadata, verification and history reopen were exercised. Responsive review led to timeline density/scroll, route scroll and dark-scrollbar improvements. Actual verification screenshot: docs/verification-ui.jpg. No full accessibility/browser matrix audit was performed.

## 12. Known limitations

Real AI access/recommendations/evidence navigation with authentic output remain unverified. Provider transport status mapping is implemented but automated tests mock the provider rather than run a mock HTTP server. Current application is local/single-user with no public deployment/auth. Dashboard aggregation is intended for small demo datasets. History filtering applies to the current page. A first QA run retains pre-fix attempt metadata because original stored evidence was not rewritten; new runs are correct. Google Fonts needs network for Inter, with system fallback. Docker Compose is supplied but not executed. PostgreSQL QA used an isolated cluster rather than credentials for the existing instance.

## 13. Security review

No secrets in frontend, API payloads, reports or tracked source. `.env`, `.tools`, data, dependencies and build outputs are ignored. Backend key import does not print values. Provider endpoint is fixed to verified HTTPS; redirects disabled; external error bodies/headers never returned. Input validation, safe exception DTOs, bounded analysis, evidence ownership and database constraints are implemented. React escapes model text. No arbitrary model code runs. Local services bind to loopback. Authentication, multi-user ownership, public deployment hardening and load limits remain required before wider exposure.

## 14. Credit management

No startup/background AI calls. One explicit investigation request per new run; saved successful analysis is reused. One process serializes investigations. Max 64 events / 24,000 trace characters, 2,000 completion tokens, 64KiB HTTP response, 32,000 analysis characters; 5s connect/30s read timeouts. Zero automatic retries. No automated test uses credits. Key rotation is manual outside source followed by restart; accounts/credits are never automatically combined or switched.

## 15. Remaining work

P0: configure an authorized Meta key and complete one controlled authenticated investigation/evidence-click demonstration. P1: mock HTTP transport tests, Docker smoke, formal accessibility/browser testing, auth/deployment controls, SQL aggregates for scale. P2: extract more React page components, add provider/result history, distributed job/dedup support if needed. See docs/REMAINING_WORK.md for reasons/modules.

## 16. Exact next steps

1. Start the app using the two scripts (QA preview services are stopped after inspection).
2. Put an authorized META_API_KEY in the backend environment or ignored .env and restart.
3. Create a new duplicate run, explicitly invoke Meta once, inspect its hypothesis/uncertainty/recommendation and follow its evidence references.
4. Run deterministic verification and reopen the result from history; rehearse the README demonstration.
5. If Docker is the submission runtime, start its engine and validate Compose separately. Add deployment security before exposing beyond loopback.

## 17. Final readiness verdict

| Question | Verdict |
|---|---|
| Can the app run locally? | YES — started and exercised on this Windows machine |
| Does the backend compile? | YES — Java 17 / Boot 3.5.15 packaged successfully |
| Do backend tests pass? | YES — 22/22 |
| Does frontend build? | YES; tests and lint also pass |
| Does frontend connect to backend? | YES — real browser simulation/verification workflow |
| Does simulator work? | YES — all scenarios verified on H2 and PostgreSQL |
| Does Meta AI work with real credentials? | NOT VERIFIED — no key supplied |
| Was live UI visually inspected? | YES — five screens plus representative mobile layouts |
| What remains before full AI submission? | One authorized real Meta request and evidence-linked end-to-end rehearsal |

The controlled simulation/verification product is demonstrable. A complete authenticated AI-assisted demonstration is not yet verified. No Git push or public deployment was performed.
