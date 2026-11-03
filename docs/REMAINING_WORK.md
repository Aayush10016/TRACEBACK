# Remaining work

## P0 — before a full AI demonstration

1. Configure an authorized `META_API_KEY` in the backend environment or ignored `.env`, restart, and explicitly investigate one new duplicate run. Validate account/model access, the response schema, displayed uncertainty/correction, and evidence-linked selection. The official contract is implemented, but real credentials were unavailable. Relevant: `MetaProvider`, `InvestigationService`, React AI panel.
2. Follow the README demo once with that key. A successful deterministic replay is already verified; it does not establish real AI access or modify production code.

## P1 — before wider use

- Add auth, authorization, rate limits, TLS/reverse-proxy configuration and operational limits before exposing beyond loopback. Current build is a local single-user tool.
- Add a mock HTTP-server suite for Meta's transport errors, response size bounds, redirects, timeout and rate-limit paths. Current automated AI tests mock the provider; transport failure mapping is implemented but live authenticated behavior is unverified.
- Test Docker Compose once Docker Desktop is running. PostgreSQL itself has already been tested with the installed binaries and an isolated database.
- Perform automated accessibility checks and keyboard/screen-reader regression testing; broaden mobile and browser coverage. Existing QA covered representative laptop and phone dimensions.
- Replace dashboard full-run aggregation with SQL aggregates and add database pagination/filter search for larger datasets. Current filtering is clearly labeled as applying to the current page.

## P2 — optional improvements

- Extract page components/hooks from `frontend/src/App.tsx` as the product grows.
- Add selectable investigation versions and model-usage metadata; current successful analysis is cached per run and latest verification is reopened while older executions remain stored.
- Add background jobs, cancellation, and database-backed request deduplication if multiple server processes/users are introduced. Current paid investigations are serialized in one process.
- Extend scenarios and optionally ingest real logs through a separately validated import interface. Current scope is controlled order simulation.
- Expand the initial Figma direction composition into a reusable component library if design handoff is needed.

An early QA run created before the retry-metadata correction remains immutable in local H2 history. New runs have correct attempt-2 retry metadata. Use a newly created run for the final demonstration; original stored evidence was not rewritten.

## Superseding NVIDIA status — 2026-10-30

The P0 real-AI demonstration above has now been completed using NVIDIA-hosted Meta Muse Glimmer: authenticated analysis, five valid evidence references, live evidence-link navigation and deterministic verification passed. Direct Meta API access is still unavailable/unverified, but is not required for the currently configured NVIDIA route. The P1 provider transport, accessibility and deployment improvements still apply. See the current update at the top of REPORT.md for actual attempts and results.

## Superseding release status — 2026-11-03

Combined frontend/backend deployment JAR, NVIDIA-only production setup, shared password gate and cross-origin write protection are implemented and tested. Timeline/mobile defects and recovery flows fixed. Public hosting and final HTTPS/host checks still remain. Docker engine is unavailable; image not built. The shared gate is not per-user auth or comprehensive quota control. Formal accessibility, broad browser/load tests and provider HTTP-server transport suites remain future work.

## Uploaded project update — 2026-11-03

Java 17 Maven/Spring ZIP upload and real Docker execution are now implemented/tested. Public uploaded-code hosting still needs a dedicated Docker-capable host or separately reviewed worker architecture; no web-container control-socket mount was approved. Add disk/retention quotas, per-user authorization and stronger hostile-workload isolation before multi-tenant use. Other build systems/runtimes and automatic per-file test generation are not supported.
