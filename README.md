# TRACEBACK

Every failure has a cause. Find it, prove it, and prevent it from happening again.

A local incident investigation application for deterministic e-commerce simulations. Java 17 / Spring Boot 3.5.15, React / TypeScript / Vite, JPA / Flyway, PostgreSQL or persistent H2. All source and runtime data stay under `D:\TRACEBACK`.

## Quick start (Windows PowerShell)

Prerequisites: Java 17 on PATH, Node 22.12+ (Node 24 tested), npm, Git, and an internet connection for first dependency installation. Scripts bootstrap Maven **3.9.16** into `.tools`; no global tool configuration is changed. PostgreSQL is optional for the local H2 profile.

Open two terminals:

```powershell
Set-Location D:\TRACEBACK
.\scripts\start-backend.ps1
```

```powershell
Set-Location D:\TRACEBACK
.\scripts\start-frontend.ps1
```

Open http://127.0.0.1:5173. Backend: http://127.0.0.1:8080/api/health. Stop each service with Ctrl+C. Stop the backend before a clean Maven build on Windows, where the running JAR is locked.

The backend script compiles before launch and sets a short project-local temporary directory, avoiding the installed Java runtime's Windows Unix-domain socket path failure. Local H2 data lives in `backend/data/traceback.mv.db`; restarting preserves runs. Do not delete the data directory to troubleshoot a build.

## Install and test explicitly

```powershell
Set-Location D:\TRACEBACK\frontend
npm ci
npm run test
npm run lint
npm run build
Set-Location D:\TRACEBACK
.\scripts\run-tests.ps1
```

The test script executes Maven `clean verify`, then frontend tests, lint and production build. It does not load `.env` or make paid AI calls. Maven can also be run directly:

```powershell
Set-Location D:\TRACEBACK\backend
..\.tools\apache-maven-3.9.16\bin\mvn.cmd '-Dmaven.repo.local=D:\TRACEBACK\.tools\m2' --no-transfer-progress clean verify
```

## Demonstration

1. On Overview, choose **Start a simulation** → **Duplicate order** → **Run simulation**.
2. Inspect the nine stored events: payment succeeds, the first order commits, the response times out, and an unsafe retry creates another order. Select events and expand metadata.
3. With NVIDIA configured, explicitly choose **Investigate trace**. The backend validates JSON and event ownership before saving. Click an evidence reference to select its stored event. Without credentials, the UI explains the configuration requirement; there is no fabricated analysis.
4. Choose **Run verification**. The original remains two orders / one logical order / one duplicate. A separate replay uses an idempotency lookup and produces one order / one logical order / zero duplicates. Inspect all five checks and the replay trace.
5. Open **Run history**, reopen the run, or refresh the browser. The verification persists.
6. Run **Normal order** (one order), **Payment failure** (zero orders), and **Timeout with idempotency** (one order). Their verification checks confirm scenario invariants; a payment failure is expected behavior, not a successful payment.
7. Open **System status** to inspect backend, database and NVIDIA readiness.

CLI smoke check (creates four additional real runs):

```powershell
.\scripts\smoke-test.ps1
```

## NVIDIA configuration

Copy `.env.example` to `.env` only if `.env` does not exist. Set `NVIDIA_API_KEY` there and keep `AI_PROVIDER=nvidia`, `NVIDIA_MODEL=meta/muse-glimmer-30b`. Restart the backend through the script, which loads `.env` without printing values. Never overwrite an existing saved key. Direct Java launches require environment variables; Spring does not import `.env` automatically.

This project uses NVIDIA-hosted Meta Muse Glimmer. No direct Meta key is needed. A real authenticated analysis and linked evidence were tested previously; quota and future availability depend on NVIDIA. Keys stay on the server, outside Git and frontend assets.

## PostgreSQL

Use an existing database/account you control. Create an empty `traceback` database and a dedicated role using your normal PostgreSQL administration tools. Set `DB_URL`, `DB_USER`, `DB_PASSWORD` in `.env`, then:

```powershell
.\scripts\start-backend.ps1 -Profile postgres
```

Flyway applies `V1__initial_schema.sql`, and Hibernate validates it. The local profile remains H2 unless `postgres` is selected. No existing database is modified by the application until you deliberately select that profile and configure its URL.

Optional Docker path: start Docker Desktop, set a non-placeholder `DB_PASSWORD` in `.env`, run `docker compose up -d`, and set `DB_URL=jdbc:postgresql://127.0.0.1:55432/traceback`. Compose binds to loopback and uses a named persistent volume. Do not run volume-deleting commands on data you need. Docker was stopped during implementation; Compose was not live-tested.

An isolated PostgreSQL 17 QA cluster was used on port 55433 during verification; its results are documented in REPORT.md. It is separate from the user's pre-existing PostgreSQL service.

## Troubleshooting

- Backend unavailable: inspect its terminal, check port 8080, and restart through the provided script. Do not start two backends against one H2 file.
- `Unable to establish loopback connection` / `Invalid argument: connect`: use the startup script's short `.tools/tmp` path. Changing the selector provider alone did not fix this environment.
- Maven cannot delete the JAR: stop the running backend and repeat `run-tests.ps1`.
- Frontend cannot proxy: start the backend first; use port 5173 and `/api` through Vite. For deployment, use the combined release JAR or Dockerfile described in `docs/DEPLOYMENT.md`; static assets alone do not run Java.
- PostgreSQL authentication failure: supply the correct local credentials; the app does not discover or reset passwords.
- NVIDIA not configured: import `.env` through the startup script and restart. A present key still requires an explicit authenticated check.
- Invalid analysis or evidence: rejected analysis is not saved. Correct provider/model configuration before retrying. No automatic model calls occur.
- No data: create a simulation. Metrics are computed from the database; no fixtures are seeded into production storage.

See [REPORT.md](REPORT.md), [architecture](docs/ARCHITECTURE.md), [API](docs/API.md), [testing](docs/TESTING.md), [design system](docs/DESIGN_SYSTEM.md), and [remaining work](docs/REMAINING_WORK.md). The complete uploaded specification is preserved as `MASTER_SPECIFICATION.md`.

## NVIDIA-hosted Meta model (added 2026-10-30)

For an NVIDIA API Catalog key, edit the ignored `D:\TRACEBACK\.env`:

```dotenv
AI_PROVIDER=nvidia
NVIDIA_API_KEY=your_authorized_nvidia_key
NVIDIA_MODEL=meta/muse-glimmer-30b
```

Restart `scripts/start-backend.ps1` after saving. This uses NVIDIA to host a Meta Muse Glimmer model; it is not a direct Meta Model API request. The UI and stored investigation record identify `nvidia` and the actual configured model. A safe provider-neutral status is available at `/api/health` under `ai`; `meta` remains a compatibility alias for older clients.

The implementation follows [NVIDIA's official Muse Glimmer API reference](https://docs.api.nvidia.com/nim/reference/meta-muse-glimmer-30b-infer): fixed `POST https://integrate.api.nvidia.com/v1/chat/completions`, bearer authentication, system/user messages, `max_tokens: 2000`, `temperature: 0.95`, `stream: false`. JSON is requested in the system prompt and validated by Java; undocumented JSON output parameters are not sent to this endpoint. No claim is made that account credits or model access are available until a real request succeeds. Pending HTTP 202 is reported explicitly without polling or automatic retries.

The supported demo setup uses NVIDIA only. Legacy direct-Meta code is retained for existing history and tests; it is not used by the production profile.


The live catalog check on 2026-10-30 supersedes the initial Llama 3.3 selection: that older model returned HTTP 410 Gone. The selected model is meta/muse-glimmer-30b, verified against NVIDIA's live /v1/models catalog and its official Muse Glimmer reference. The first Muse request timed out at 30 seconds; the client now allows a bounded 90-second read window (5-second connect), and the frontend allows 100 seconds for investigation POSTs. No automatic model fallback or retry occurs.

## Deploy the combined application

See [Deployment guide](docs/DEPLOYMENT.md). `scripts/build-release.ps1` builds the frontend and embeds it in the Java JAR, runs all checks, and produces one deployable service. The production profile requires shared demo credentials and keeps NVIDIA credentials server-side. Do not deploy the Vite development server.
