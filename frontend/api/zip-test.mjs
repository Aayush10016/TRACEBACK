import { readFile } from "node:fs/promises";
import { Sandbox } from "@vercel/sandbox";

const MAX_BYTES = 4 * 1024 * 1024;
const ORIGIN = "https://traceback-lab.vercel.app";
const MAVEN = "/tmp/apache-maven-3.9.16/bin/mvn";
const MAVEN_ARCHIVE = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.16/apache-maven-3.9.16-bin.tar.gz";
let inProgress = false;
let nextRun = 0;

function reply(data, status = 200) {
  return Response.json(data, { status, headers: { "Cache-Control": "no-store" } });
}

export default {
  async fetch(request) {
    if (request.method !== "POST") return reply({ message: "Upload a Maven ZIP using POST." }, 405);
    if (request.headers.get("origin") !== ORIGIN) return reply({ message: "Start the test from TRACEBACK." }, 403);
    if (request.headers.get("content-type") !== "application/zip") return reply({ message: "Choose a ZIP file." }, 415);
    if (inProgress || Date.now() < nextRun) return reply({ message: "The public runner is busy. Try again in a minute." }, 429);
    const zip = Buffer.from(await request.arrayBuffer());
    if (zip.length > MAX_BYTES) return reply({ message: "The hosted ZIP limit is 4 MB." }, 413);
    if (zip.length < 22 || zip.readUInt32LE(0) !== 0x04034b50) return reply({ message: "Choose a valid nonempty ZIP." }, 400);
    if (inProgress || Date.now() < nextRun) return reply({ message: "The public runner is busy. Try again in a minute." }, 429);
    inProgress = true;
    nextRun = Date.now() + 30_000;
    let sandbox;
    try {
      sandbox = await Sandbox.create({ runtime: "python3.13", timeout: 225_000, resources: { vcpus: 1 }, persistent: false });
      const java = await sandbox.runCommand({ cmd: "timeout", args: ["75s", "sh", "-ec", "if command -v dnf >/dev/null; then dnf -qy install java-17-amazon-corretto-devel; else apt-get -qq update && DEBIAN_FRONTEND=noninteractive apt-get -qy install openjdk-17-jdk-headless; fi"], sudo: true });
      if (java.exitCode !== 0) return reply({ message: "The cloud runner could not initialize Java 17." }, 503);
      const maven = await sandbox.runCommand({
        cmd: "sh", args: ["-ec", "curl --fail --silent --show-error --max-time 30 \"$1\" -o /tmp/maven.tgz; curl --fail --silent --show-error --max-time 15 \"$1.sha512\" -o /tmp/maven.sha512; printf '%s  /tmp/maven.tgz\\n' \"$(cat /tmp/maven.sha512)\" | sha512sum -c -; tar -xzf /tmp/maven.tgz -C /tmp", "maven-bootstrap", MAVEN_ARCHIVE],
      });
      if (maven.exitCode !== 0) return reply({ message: "The cloud runner could not initialize Maven." }, 503);
      await sandbox.update({ networkPolicy: { allow: ["repo.maven.apache.org"] } });
      await sandbox.writeFiles([
        { path: "traceback-runner.py", content: await readFile(new URL("../sandbox/maven-runner.py", import.meta.url)) },
        { path: "traceback-upload.zip", content: zip },
      ]);
      const run = await sandbox.runCommand({ cmd: "python3", args: ["traceback-runner.py", "traceback-upload.zip", "traceback-project", MAVEN] });
      if (run.exitCode !== 0) return reply({ message: "The sandbox ended before it could report test results." }, 503);
      const result = JSON.parse(await run.stdout());
      return reply(result, result.status === "INVALID_ZIP" ? 400 : 200);
    } catch (error) {
      console.error("TRACEBACK sandbox request failed", error instanceof Error ? error.name : "UnknownError");
      return reply({ message: "The cloud runner is unavailable or its free quota is exhausted. Try again later." }, 503);
    } finally {
      if (sandbox) await sandbox.stop().catch(() => {});
      inProgress = false;
    }
  },
};
