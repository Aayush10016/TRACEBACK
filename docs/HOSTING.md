# TRACEBACK on Render and Vercel

Use the existing private Aayush10016/TRACEBACK GitHub repository. Display name: **TRACEBACK**. Service and frontend project name: **traceback-lab**, if available. Keep the default hostnames for the hackathon unless you already own a domain you want to connect.

## 1. Windows test worker

Start Docker Desktop and make sure the image traceback-java-runner:17 exists; run scripts/build-runner.ps1 if needed. Set WORKER_USERNAME and WORKER_PASSWORD (16 or more characters) in the ignored D:/TRACEBACK/.env file. Use credentials distinct from the demo login.

In PowerShell from D:/TRACEBACK, run scripts/build-worker-release.ps1 and then scripts/start-worker.ps1. The worker listens on loopback port 8085 and keeps its test history separate from the original app. Confirm /actuator/health returns UP and /api/projects returns 401 without credentials. Do not start the tunnel until both checks pass.

Use the official cloudflared executable: cloudflared tunnel --url http://127.0.0.1:8085. Copy the generated HTTPS trycloudflare.com origin. No router port forwarding is needed. The tunnel is temporary and restarting it can change the URL. Leave the PC awake and connected, and Docker Desktop, the Java worker and cloudflared running through judging.

## 2. Render backend

Grant the Render GitHub integration access to only the TRACEBACK repository in addition to its current selections. In Render, choose New > Blueprint, select TRACEBACK on main, and review the free Web Service and free PostgreSQL database in render.yaml. If the workspace already has a free PostgreSQL instance, the Blueprint cannot create another free one in that workspace. Don't delete any existing database.

Enter these in Render only:

- NVIDIA_API_KEY: the existing NVIDIA key from your local .env.
- DEMO_USERNAME and DEMO_PASSWORD: separate judge login; password must be 16 or more characters.
- PROJECT_WORKER_URL: the HTTPS tunnel origin.
- WORKER_USERNAME and WORKER_PASSWORD: the same credentials used by the Windows worker.
- DEMO_PUBLIC_ORIGIN: the exact Vercel production origin, no trailing slash. If the eventual Vercel URL differs from the planned name, update this value and redeploy.

The Blueprint links database settings automatically. After the deploy, confirm the actual Render HTTPS origin serves /actuator/health with status UP. Render Free can sleep; PostgreSQL preserves simulation history across web restarts, but the free database expires after 30 days. The Windows worker holds ZIP files and project test results.

## 3. Vercel frontend

Run scripts/configure-vercel.ps1 -BackendUrl with the actual HTTPS Render origin and commit/push the generated frontend/vercel.json. Import the same private GitHub repository into Vercel Hobby. Set project name traceback-lab if available, root directory frontend, framework Vite and output dist. Use the production URL and update DEMO_PUBLIC_ORIGIN in Render to match. No NVIDIA or worker credentials go to Vercel. The app sends same-origin /api requests through a rewrite to Render.

## 4. Verify and record

Open the production frontend in a new private browser window. Sign in using the demo login, confirm the runner is available, upload a small sample Maven ZIP, and wait for an actual passing or failing test result. Next create a simulation, run an NVIDIA investigation, and verify the fix. Confirm the evidence links. A new Render database does not contain the existing local NVIDIA analysis.

Use the actual Render HTTPS /actuator/health URL as a five-minute UptimeRobot HTTP monitor. It checks the backend only, without login or model usage. The frontend can be monitored separately. Neither monitor keeps the PC awake or guarantees that the worker and tunnel are online.

Keep the NVIDIA key and worker credentials out of the video and submission. Provide only the demo sign-in to judges through the submission's private access field.
