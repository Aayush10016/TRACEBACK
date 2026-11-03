# Deploy TRACEBACK for the hackathon

Current demo uses NVIDIA-hosted Meta Muse Glimmer only. It is a Java service with the built React website embedded in its JAR. A static-only host cannot run the backend. Do not expose Vite's development server.

## Deploy on a Docker-capable host

1. Use this repository's `Dockerfile` as the build entry point. It builds React, runs backend tests and packages the combined service. No local key or database is included in the image; `.dockerignore` excludes both. The Docker image itself has not been built in this Windows environment because Docker's engine is stopped. The equivalent combined JAR is tested separately.
2. Set these environment variables in the host's private secret/environment settings:
   - `SPRING_PROFILES_ACTIVE=production` (already set by the Dockerfile)
   - `NVIDIA_API_KEY`: your existing authorized NVIDIA key
   - `NVIDIA_MODEL=meta/muse-glimmer-30b`
   - `DEMO_USERNAME`: a demo sign-in name
   - `DEMO_PASSWORD`: a unique password of at least 16 characters, shared privately with judges
   - `PORT`: the port provided by the host, or 8080
3. Attach a persistent disk at `/app/data`. It must be writable by the container's `traceback` user. Run **one instance** with H2. A host with ephemeral storage will lose runs when replaced.
4. Use the host's HTTPS URL/reverse proxy. Forward the original scheme and host; do not expose the raw Java port directly to the Internet. Proxy timeouts must allow at least 110 seconds for a bounded NVIDIA response.
5. Health-check `/actuator/health`, which returns only basic service health without sign-in. The website, assets and `/api` require demo sign-in in the production profile. The browser shows its normal username/password prompt. A public health probe does not establish model access.
6. Open the HTTPS URL, sign in, run a simulation, inspect events, run verification, then explicitly investigate a run if you want to spend NVIDIA quota. Test the resulting evidence links. Fresh storage begins empty; the app does not fabricate or seed AI results.
7. Record the demonstration and provide judges the website URL and demo credentials through the submission's private access field.

No host account, paid service, Git push or public deployment has been created by this work.

## Deploy the prebuilt JAR instead

The release is `backend/target/traceback-1.0.0.jar`. A copy is supplied with this handoff. Install Java 17, set the variables above and launch:

```text
java -jar traceback-1.0.0.jar
```

Set `DATA_DIR` to an absolute persistent data directory if launching outside the Docker image. Java launched directly does not import `.env`; use your host's environment/secret settings. Local Windows startup scripts do import the existing ignored `.env`.

To rebuild on Windows, stop the backend first (Windows locks its JAR), then run `scripts/build-release.ps1`. The script executes frontend tests/lint/build and Maven `-Prelease clean verify`. This embeds `frontend/dist` as Spring static resources. Opening `/` and `/api` now uses a single origin; no separate frontend URL or CORS changes are required.

## Optional PostgreSQL

Set `SPRING_PROFILES_ACTIVE=production,postgres` (in that order), `DB_URL` to a JDBC PostgreSQL URL, `DB_USER` and `DB_PASSWORD` in host settings. Use the database provider's TLS requirements. The later postgres profile replaces H2 properties. The existing PostgreSQL adapter was tested earlier; this pass tests production H2. Do not copy an H2 file into PostgreSQL. Moving historical runs requires a deliberate migration.

## Existing saved analysis

Your local demonstration run is `fd13d542-96e3-4527-b564-696d91496078`. Its NVIDIA analysis and verification are real, saved results; reopening it makes no generation request. It lives in the local H2 database, not in source or the release JAR. A newly deployed empty disk does not contain it. Create and investigate a new run after deployment, or arrange a database backup/transfer while the local backend is stopped. Never copy a live, open database file.

## Access and limits

Production refuses startup without valid demo credentials. Basic authentication requires HTTPS. It is a shared hackathon gate, not separate user accounts: everyone with access shares stored runs and can create investigations that consume your NVIDIA quota. Keep credentials private, use host/provider spending limits, and rotate the demo password after judging. Cross-origin browser writes are rejected. Public multi-user service, per-user permissions and comprehensive rate limiting remain outside this demo release.

NVIDIA keys belong only on the server. Do not put them in frontend settings, Git, a source archive, the demo video or submission text. Model availability/quota may change; a configured key is distinct from a successful generation.
