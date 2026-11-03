# Test your own Java / Spring project

The default Project tests screen runs uploaded Maven projects. The original controlled simulator remains separately labeled Simulation lab. Uploaded-project results are independent of simulator verification and do not call NVIDIA.

## Use it now

1. Keep Docker Desktop running. The image `traceback-java-runner:17` must exist; build it with `scripts/build-runner.ps1` after installing on another machine.
2. Open http://127.0.0.1:8080/#view=projects. Choose a ZIP containing one Java 17 Maven project, including `pom.xml`, source and test files. A wrapping folder and Maven parent/multi-module layout are accepted. Separate independent roots are rejected so the runner never silently chooses the wrong project.
3. Remove `.env` files and private credentials. Maximum ZIP size 20 MB, extracted contents 100 MB, 3,000 entries. Archive traversal, absolute paths, duplicate paths and invalid archives are rejected. Java extraction creates ordinary files; archive links do not become host links.
4. Click Upload project and inspect the complete file inventory and selected Maven root. Upload alone never executes code.
5. For the first test of a new upload, enable the dependency-preparation checkbox. This expressly allows a separate Maven container to download dependencies; Maven plugins may execute during preparation. Without it, the build uses only this upload's existing cache. If a network transfer fails, the result says DEPENDENCY_FAILED; retry explicitly after checking the connection.
6. Click Run project tests. The actual test phase runs `mvn -o -B -ntp -Dmaven.repo.local=/cache clean verify` with network disabled. Uploaded `target` directories are discarded, so old uploaded reports cannot establish a pass.
7. Inspect counts and Maven output. PASSED requires exit zero, at least one non-skipped reported test and zero reported failures/errors. Maven's failure-ignore flag cannot turn failing reports into a pass. NO_TESTS, TESTS_FAILED, BUILD_FAILED, DEPENDENCY_FAILED, TIMED_OUT, ERROR and INTERRUPTED remain distinct.

It compiles the project's configured source modules and runs its existing Maven tests. It does not generate a meaningful test for every uploaded file, execute arbitrary non-Java files, support Gradle, provide Java 21+, or supply external databases/services during offline tests. Projects needing those capabilities may fail and show their actual output. Spring tests that use only in-process dependencies are supported. Maven wrappers from the ZIP are not executed; the runner supplies Maven 3.9.16 / Java 17.

## How execution works

Each execution has a container-owned input volume, workspace, cache and report volume. Input is copied with Docker CLI into that volume, avoiding host bind mounts and the app's database/secret environment. Containers use UID 1000, a read-only root filesystem, dropped capabilities, no-new-privileges, 1.5 GB memory, 2 CPU and 256 PID limits. Only dependency preparation has bridge networking; tests use network none. Preparation is bounded to five minutes per stage, tests to ten minutes per attempt, with up to two explicit dynamic-dependency resolution cycles under the same opt-in. JVM native temporary files use the owned workspace; `/tmp` protections remain unchanged.

Maven sometimes requests a test provider only at test time. If opted in, a narrowly parsed missing artifact coordinate is downloaded in the preparation container and the test phase is rerun offline, at most twice. The output preserves these steps. There is no AI/model retry or model charge.

Logs are bounded to 1 MB per process. JUnit Surefire/Failsafe XML reports are parsed with external entities/DTDs disabled. Jobs and results persist in the application database; source/output live under `backend/data/projects` locally. Temporary containers/input/work/report volumes are cleaned after execution. Per-upload dependency cache volumes remain for explicit reruns. Disk quotas, upload retention controls, per-user accounts and hostile multi-tenant execution are not implemented; this is a private, password-protected project-testing demo.

## Deployment boundary

**Recommended for upload execution:** run the combined Java JAR on a dedicated Docker-capable machine, under the operator's existing Docker environment, behind HTTPS and the production demo gate. The tested local Windows arrangement follows this model. Use persistent database/project storage. The operator must provision Docker and the runner image.

The Docker web image itself is built and tested, but intentionally has no Docker CLI/control-socket access. It can store uploads and serve the simulation/UI; its runner status is unavailable and execution is disabled. Simply putting this image on a static/serverless host does not provide uploaded-code execution.

A proposal to mount the Docker control socket into the web container was rejected by automatic approval because it would grant broad host/container control. No such mount or Docker CLI access was added to the web image. Public container-based execution needs a separately reviewed worker/deployment design; do not bypass this boundary by mounting the socket yourself without understanding and approving that access.

## API

- GET `/api/projects`: latest 20 persisted jobs.
- GET `/api/projects/runner`: safe runner availability, image and supported runtime.
- POST `/api/projects`: multipart field `file` containing a ZIP; returns the inspected READY job.
- GET `/api/projects/{id}`: persisted job/result.
- POST `/api/projects/{id}/test`: JSON `{ "downloadDependencies": true }` to start explicitly. One execution per application process at a time; competing starts return 409. Poll GET until terminal status.

Uploaded code and test output are not sent to NVIDIA. Do not include real credentials in ZIPs or test output. The app's NVIDIA key is not passed into runner containers.
