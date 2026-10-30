# Testing

Verified on 2026-10-29 on the user's Windows machine. Source: `D:\TRACEBACK`.

## Automated checks

From the project root, `scripts/run-tests.ps1` runs:

```text
.tools/apache-maven-3.9.16/bin/mvn.cmd -Dmaven.repo.local=D:\TRACEBACK\.tools\m2 --no-transfer-progress clean verify
npm run test
npm run lint
npm run build
```

Backend: **22 tests, zero failures/errors/skips**. `SimulatorTest` has 11 tests: normal order, duplicate reproduction, payment failure, protected retry, precise event sequence/timestamps, deterministic identical inputs, isolated histories, preserved before/repaired after values, deliberately invalid recorded outcome fails verification, retry attempt metadata, and all scenario invariants. `ApiTest` has 11 tests: persisted history/events/orders, invalid/missing scenario, bounded pagination, unknown/malformed IDs, persisted verification, absent analysis, mocked analysis acceptance/caching, foreign-run evidence rejection without persistence, malformed JSON rejection, and missing-key behavior without a network call.

Frontend: **4 interaction tests passed** in `App.test.tsx`: honest empty state, launching a simulation with a real-shaped API response and inspecting its event, safe system-status configuration, backend failure with disabled action. Provider/network responses in tests are mocks. Test fixtures are never seeded into the running application.

Lint: passed. TypeScript and Vite production build: passed. Final build output: approximately 258.35kB JS (80.01kB gzip), 25.11kB CSS (6.67kB gzip). Lockfile records exact installed versions. Prettier and Google Java Format were used for maintainable source formatting.

Evidence logs (ignored by Git, retained locally): `docs/validation-final.log`, `docs/frontend-test-final.log`, `docs/frontend-lint-final.log`, `docs/frontend-build-final.log`. Surefire XML/text live in `backend/target/surefire-reports` and are recreated by testing.

## Live API checks

`scripts/smoke-test.ps1` creates each of the four scenarios, retrieves stored events, checks actual order counts, and executes verification. It does not call Meta or delete runs.

| Scenario | Orders | Events | Verification |
|---|---:|---:|---|
| NORMAL_ORDER | 1 | 5 | PASSED |
| DUPLICATE_ORDER | 2 | 9 | PASSED after protected replay |
| PAYMENT_FAILURE | 0 | 4 | PASSED expected failure invariants |
| TIMEOUT_WITH_IDEMPOTENCY | 1 | 8 | PASSED |

All four passed against persistent H2 on port 8080 and isolated PostgreSQL 17.6 on port 8081 / database port 55433. Result IDs are recorded in `h2-smoke-results.json` and `postgres-smoke-results.json`. PostgreSQL Flyway migration and Hibernate schema validation succeeded. These checks are real HTTP/database integration checks, not the JUnit suite reconfigured to PostgreSQL.

H2 verification created before a backend restart was successfully reopened and remained PASSED after restart. A live POST investigation without a key returned HTTP 503. No real authenticated Meta request was sent.

## Live UI inspection

The in-app browser was used to inspect Overview, Investigation, Verification, History and System Status. The workflow launched a duplicate simulation, selected actual events, expanded metadata, ran deterministic verification, checked measured before/after values, reopened persisted history, and viewed honest missing-AI configuration. Desktop layout was inspected at normal laptop size and 1366×900. Overview/status were also inspected at 390×844; document width did not exceed the viewport. Long tables scroll within their container.

UI fixes from inspection: more compact/scrollable timeline, route scroll reset, dark scrollbar styling, stale run navigation correction. Screenshot: `verification-ui.jpg`.

The real AI analysis panel and evidence-reference clicks with authenticated model output were **not verified**. Backend tests validate references using mocked responses. Formal accessibility/contrast audits, screen-reader tests, browser matrix, real rate-limit/credit exhaustion, and production deployment are not verified.

## Failures encountered and resolved

- PowerShell split an unquoted Maven `-D` argument. Quoting the complete option fixed it.
- An interface mock replaced the concrete Meta provider bean required by the controller. Mocking the concrete provider fixed the test context; all tests then passed.
- Installed Java 17 failed to create its Windows Unix-domain wakeup socket under the long generated TEMP path. A short project-local temporary path fixed startup. Changing selector provider alone did not.
- Maven clean could not remove a JAR held by the running Windows backend. Stopping that preview process fixed the build; scripts/docs explain this.
- Vite restarted during dependency/configuration formatting and interrupted the preview tab. Restarting Vite and opening a fresh tab restored QA. This did not affect the build output or persisted backend data.
- PostgreSQL initdb initially received a malformed PowerShell option. Passing the complete pwfile option as one argument fixed initialization.

## Repeating checks

Stop the running backend before `run-tests.ps1`. Use an empty dedicated PostgreSQL database and the postgres profile for live smoke checks. Never use a production database for this demonstration. No automated test spends API credits or requires Meta credentials. A real-model integration check should be one explicit UI investigation on a new run after configuring an authorized key.

## NVIDIA update — 2026-10-30

Final `scripts/run-tests.ps1`: **32 backend tests passed**, **5 frontend interaction tests passed**, lint passed, production build passed. The additional backend tests cover NVIDIA's documented request fields, safe missing-key/status handling, normal/truncated/malformed completion parsing, safe HTTP errors, router isolation, unsupported provider selection, stored provider/model/cache metadata and provider-neutral health. The fifth frontend test validates NVIDIA labels/model/backend key variable. Automated tests never import the real `.env` or call the hosted model.

Real integration testing: three explicit bounded generation attempts (retired Llama 3.3 HTTP 410, Muse 30-second timeout, Muse success after extending the response window). NVIDIA's current model catalog was queried once. Successful Muse investigation returned five valid evidence references. The same real run passed deterministic verification, 2 original orders to 1 protected order / zero duplicates. In the live UI, Event 7 evidence navigation selected the actual stored second order event and updated the inspector. Full safe result: `nvidia-live-result.json`; screenshot: `nvidia-analysis-ui.jpg`. Direct Meta access remains unverified. No exact credit or token-usage balance was measured.
