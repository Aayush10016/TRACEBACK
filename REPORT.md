# Hosted demo preparation

- Backend tests: 53 passed. Includes authenticated project forwarding, multipart bytes, origin restrictions, offline worker responses and rejected redirects.
- Frontend tests: 14 passed. Lint and the production frontend build passed.
- Render Blueprint and Vercel rewrite generator added. The hosted route to the Windows worker is not verified against live Render or Vercel until both deployments are configured and a real ZIP test finishes through the production URL.
- NVIDIA integration and isolated Maven ZIP execution were verified locally in the previous release. This hosting pass did not claim a new live NVIDIA run.

---

# Real project uploads and Docker verification — 2026-11-03

Added a new default **Project tests** screen for uploaded Java 17 Maven/Spring ZIPs. It inspects and inventories the ZIP, chooses an unambiguous Maven root, and explicitly runs the project's existing Maven tests in isolated Docker containers. The original order simulator is retained as a separate **Simulation lab**, with its existing Git/data history preserved. Project tests do not call NVIDIA or send source/test output to a model.

Backend: persistent project jobs (Flyway V2), multipart upload validation, bounded extraction, path/credential-file rejection, safe XML report parsing, explicit test start and polling, actual command/output/counts and honest result categories. Old uploaded `target` outputs are discarded. A pass requires zero process exit, non-skipped tests and zero reported failures/errors, including when Maven ignores failures. CPU/memory/PID limits, UID 1000, dropped capabilities, read-only root and offline test networking apply to runner containers. Optional separate dependency preparation has network access and bounded dynamic-provider resolution. Temporary runner containers/work/report/input volumes are removed; per-upload Maven cache remains. This is private demo execution, not hostile multi-tenant hosting.

Actual checks: **48 backend tests passed; 11 frontend tests passed; lint and production build passed.** New backend checks cover ZIP paths, credential exclusion, empty archives, JUnit counts, XML entity rejection and fail-closed classification. New frontend checks cover real FormData uploads, explicit execution/consent and errors. Ignored logs: `.tools/upload-final-package.log`, `upload-final-backend-tests.log`, `upload-final-frontend-tests.log`.

Real ZIP execution outcomes (safe summary `docs/uploaded-project-results.json`): Java/JUnit fixture PASSED (1/0 failures), intentionally failing fixture TESTS_FAILED (1/1 failure), no-test fixture NO_TESTS (zero tests), Spring Boot 3.5.15 application context PASSED (1/0 failures). All three uploaded files in each executable fixture were inventoried; the no-test ZIP contained two. Failing/no-test QA caches were seeded with already-downloaded public Maven artifacts to avoid duplicate downloads. A transient Maven download truncation initially produced DEPENDENCY_FAILED; it was not hidden or marked passed. Test-provider and managed dependency-resolution issues found during real execution were fixed, followed by successful offline runs. First source-copy attempt failed against a read-only container root; container-owned input volumes fixed that without host bind mounts. JNI temporary files use the owned workspace.

Docker Desktop was started successfully (engine 29.9.2). Both the web image and Java runner image were built. Docker web runtime checks confirmed password protection, bundled website, simulation verification and persistent storage across restart; see `docs/docker-runtime-results.json` for the checked image and final HTTP results. The web image does not have Docker control access and reports its uploaded-code runner unavailable. The local Java JAR uses the operator's already-existing Docker installation and the uploaded-project workflow was tested there. No Docker socket was mounted into the web container: automatic approval rejected that persistent host-control expansion. Recommended uploaded-code deployment is the Java JAR on a dedicated Docker-capable host; public container worker hosting requires a separate reviewed design.

No new model requests, public deployment, Git push or account creation. Real user project ZIPs have not yet been supplied; the four end-to-end cases above are clearly labeled QA fixtures. Source archives and release files exclude `.env`, uploaded sources, local databases and tool caches. Gradle, standalone files, Java 21+, generated tests for every file, external test databases/services and exhaustive security/accessibility/load coverage are not claimed. See `docs/PROJECT_UPLOADS.md` for supported inputs, commands, limits and setup. Earlier reports below are historical.

---

# Website repair and release — 2026-11-03

Reviewed the five application screens against the user's reported timeline overlap and stale Meta-key setup. Fixed the underlying bounded-timeline overflow, full-width inspector layout, mobile navigation, wrapped headers/identifiers, clipped mobile verification results and backend-recovery/deep-link Retry behavior. Old success notices are cleared when loading another run. Timeline and history scroll regions are keyboard-focusable; the skip link now focuses main content without changing the route. Event descriptions/metadata use more readable type and colors.

The active setup and production profile use NVIDIA-hosted Meta Muse Glimmer only. No Meta key is needed. Configured users no longer see key setup instructions. Offline status never falls back to Meta configuration. Status distinguishes saved real analysis from the current server's configured credentials. Legacy direct Meta code and historical results remain preserved; the production route is explicitly NVIDIA.

## Actual validation

- Final release build: **37 backend tests passed**, no failures/errors/skips; **8 frontend interaction tests passed**; lint and TypeScript/Vite build passed. Added five access-protection tests and two recovery regressions. Final log: ignored `.tools/release-validation.log`.
- Real production-profile smoke: bundled React index and JS served by Java, unauthenticated root 401, authorized access succeeds, NVIDIA status, duplicate simulation and deterministic verification 2→1 orders, foreign-origin POST 403, saved run and verification reopen after an abrupt restart. Safe result: `docs/release-smoke-results.json`. A first abrupt-restart check exposed delayed H2 flushing; production now uses `WRITE_DELAY=0`, and the repeated test passed.
- Responsive checks: all five screens at 320, 768, 1024 and 1366px; visually inspected all five at 390px and desktop. No page-wide overflow at these sizes. Timeline footer remains outside the scrolling event list. Saved real NVIDIA Event 7 scrolls into view and selects the correct second order; metadata expands without widening the phone page. History no-match/clear, modal Escape/focus return, replay expansion and new simulation were exercised.
- New phone UI run `292e021e-dae3-4176-a56d-e86915d02a07`: timeout with idempotency, 8 events, 1 order, zero duplicates, deterministic verification PASSED. No new model generation requests were made. Previously authenticated NVIDIA evidence remains stored.
- The combined Java release is the deployment artifact; React is embedded by Maven's release profile. Added a multistage Dockerfile, private-context exclusions, production profile, password-protected demo gate and deployment instructions. The gate is shared demo access, not a multi-user account system.
- Docker engine checked and unavailable, so container image build/start remains untested. No public hosting, Git push, provider account creation or spending was performed. Public HTTPS/host-specific networking must still be checked at deployment. Formal screen-reader/accessibility compliance, broad browser/device coverage and production-scale load testing are not claimed.

## Handoff

Project and Git history remain at `D:\TRACEBACK`. Local final website can be served directly at http://127.0.0.1:8080/ from `backend/target/traceback-1.0.0.jar`; Vite development preview remains available at port 5173. `docs/DEPLOYMENT.md` describes deploying the combined service with a persistent disk, HTTPS and private environment variables. Never publish the existing `.env` or copy a running H2 database file. Fresh deployment storage starts empty and does not contain the local saved NVIDIA analysis automatically.

Earlier reports below are historical and superseded where this update applies.

---

# Latest UI refinement — 2026-10-30

Applied a more minimal graphite interface across all five screens. Reduced decorative gradients/cards, removed the sidebar promotional panel/version/fake avatar, simplified page labels, and introduced restrained off-white actions, neutral navigation, quieter status indicators and larger, more consistent text. The overview now prioritizes its action, real metrics and recent runs.

The investigation keeps the actual hypothesis, linked evidence and correction visible. Longer causal reasoning, verification plans and technical event metadata remain available through accessible expandable sections. The desktop timeline stays in view while reading analysis. No analysis is truncated, fabricated or replaced. The original NVIDIA integration, key configuration, persistence and deterministic verifier are unchanged.

Also fixed stale dashboard data on navigation; a new regression test confirms newly persisted counts appear when returning to Overview. Mobile QA found an absolutely positioned accessible table label escaping its scroll container; positioning the container resolved page overflow while retaining table scrolling and the label. The simulation-dialog heading was adjusted for phone readability. Configuration text is more concise; analysis status explicitly refers to the selected run.

Validation: **6 frontend interaction tests passed; lint passed; TypeScript/Vite production build passed.** Backend code was not changed or rerun for this UI-only pass; its last verified run remains **32/32 passing**. The new normal-order run `bb31cf40-4dd2-4963-ac26-d5e8d7070ed7` was launched through the phone dialog, created one order and passed live deterministic verification. Existing real NVIDIA evidence navigation still selects the correct stored event. No new model generation requests were made.

Visually checked Overview, Investigation, Verification, History and System Status at representative laptop and phone dimensions. At a 390px viewport, the document width is 380px with a scrollbar and no page overflow. Formal screen-reader/contrast audits remain unperformed. The preview initially cached an empty new stylesheet during file creation; restarting only Vite resolved it. A TypeScript error in the new test's selector options was corrected, then all final checks passed.

Files: `frontend/src/refinement.css`, `App.tsx`, `main.tsx`, `App.test.tsx`; screenshot `docs/minimal-ui-overview.jpg`; safe QA summary `docs/ui-refinement-results.json`; ignored logs `docs/ui-refinement-{tests,lint,build}.log`. Current production assets are approximately 258.09kB JS (79.98kB gzip) and 40.95kB CSS (9.84kB gzip). Services remain running at http://127.0.0.1:5173 for user testing. Earlier baseline reports follow below.

---
# Current update — NVIDIA integration verified, 2026-10-30

This update supersedes the AI-readiness limitations in the historical 2026-10-29 baseline report below.

- Added a separate NVIDIA provider and provider router; direct Meta support is preserved. NVIDIA hosts the selected **Meta Muse Glimmer 30B** model (`meta/muse-glimmer-30b`). The selected host/model are stored with investigations and displayed accurately in the UI. No account/key fallback occurs.
- The user saved the key in Git-ignored `D:\TRACEBACK\.env` as `NVIDIA_API_KEY`. `AI_PROVIDER=nvidia` and `NVIDIA_MODEL=meta/muse-glimmer-30b` select this integration. The key was never printed, placed in frontend source, sent in command-line arguments or included in this report.
- Official contract verified at https://docs.api.nvidia.com/nim/reference/meta-muse-glimmer-30b-infer, plus a live NVIDIA `/v1/models` catalog query. Fixed HTTPS chat-completions endpoint, bearer header, system/user messages, max_tokens 2000, temperature 0.95, reasoning_effort none, stream false. Strict JSON/evidence validation remains mandatory. Connect timeout is 5 seconds; NVIDIA read timeout is 90 seconds, with a matching 100-second frontend investigation window. Direct Meta retains its original timeout. No automatic retries occur.
- **Real authenticated generation succeeded.** Run `fd13d542-96e3-4527-b564-696d91496078`, investigation `d70f7c20-bb0f-4595-9ba4-579f1e9c1232`: five evidence references, all verified to belong to the stored run. The model identified timeout followed by unsafe retry and recommended atomic idempotency enforcement. Its uncertainty, alternatives and risks are retained; the model did not decide verification pass/fail.
- **Real UI evidence navigation verified:** clicking AI reference Event 7 selected the actual second ORDER_CREATED event (`1ef9009f-64a2-3b0b-ab58-36a3fc278d8d`) and updated the inspector. The AI panel displays NVIDIA and the actual Muse Glimmer model. Screenshot: `docs/nvidia-analysis-ui.jpg`.
- **Deterministic verification PASSED:** original 2 orders / 1 duplicate → protected replay 1 order / 0 duplicates, with all five checks passing. Original evidence remains intact. No external production code was changed.
- Latest automated checks: **32 backend tests, 0 failures/errors/skips; 5 frontend interaction tests passed; lint and production build passed.** Added request-format, secret-safe status, completion/truncation parsing, error mapping, provider routing, persisted provider metadata/caching, health and NVIDIA UI-label coverage. Tests mock providers and spend no credits. Evidence log: `docs/nvidia-validation.log` (ignored); safe live summary: `docs/nvidia-live-result.json`.
- Three controlled generation requests were attempted in total: initial Llama 3.3 returned HTTP 410 Gone; catalog-confirmed Muse Glimmer timed out at the initial 30-second window; the final Muse request succeeded with the bounded 90-second window. One catalog query was also made. No blind/automatic retries or fabricated results were used. Exact token usage/account credits were not measured.
- Backend, database and frontend connection are healthy. Services are intentionally **left running for the user's testing** at http://127.0.0.1:5173. Restart through the documented scripts after editing `.env`.

Current readiness: the controlled simulator, NVIDIA-hosted Meta analysis, evidence links and deterministic verification have all been exercised end to end. Direct Meta Model API remains separately unverified. Remaining improvements: HTTP-server transport tests (current transport unit tests validate safe mapping/parsing), provider usage accounting, formal accessibility coverage and production authentication/deployment hardening. Successful saved analysis is cached; reopening or refreshing this tested run does not make another model call. Creating and investigating a new run can consume NVIDIA quota.

---

# Historical baseline report — 2026-10-29
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
