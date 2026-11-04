import { useState } from "react";

type Result = {
  status: string;
  counts: { tests: number; failures: number; errors: number; skipped: number };
  output: string;
};

export default function HostedProjectWorkspace() {
  const [file, setFile] = useState<File>();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [result, setResult] = useState<Result>();

  async function runTests() {
    if (!file || busy) return;
    setError("");
    setResult(undefined);
    if (file.size > 4 * 1024 * 1024) {
      setError("Choose a ZIP under 4 MB.");
      return;
    }
    setBusy(true);
    try {
      const response = await fetch("/api/zip-test", {
        method: "POST",
        headers: { "Content-Type": "application/zip" },
        body: file,
        signal: AbortSignal.timeout(250_000),
      });
      const data = await response.json().catch(() => null);
      if (!response.ok || !data) throw new Error(data?.message || "The hosted runner did not return a result.");
      setResult(data as Result);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "The test request failed.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="panel">
      <div className="panel-heading"><h2>Test your Java project</h2></div>
      <div className="panel-body">
        <p>Choose a Java 17 Maven ZIP with a pom.xml and its existing tests. The hosted runner uses a fresh cloud sandbox for each request. Results are shown in this tab.</p>
        <p className="muted">Up to 4 MB compressed, 40 MB expanded. Use Maven Central dependencies. Remove private keys and environment files before uploading.</p>
        <label>Java Maven project ZIP <input type="file" accept=".zip,application/zip" disabled={busy} onChange={event => setFile(event.target.files?.[0])} /></label>
        <button className="primary" type="button" disabled={!file || busy} onClick={() => void runTests()}>
          {busy ? "Running Maven tests…" : "Upload and run tests"}
        </button>
        {busy && <p role="status">Preparing a cloud sandbox and running Maven. Keep this tab open.</p>}
        {error && <p role="alert">{error}</p>}
        {result && <div aria-live="polite">
          <h3>{result.status.replaceAll("_", " ")}</h3>
          <p>Tests {result.counts.tests} · Failures {result.counts.failures} · Errors {result.counts.errors} · Skipped {result.counts.skipped}</p>
          <details><summary>Maven output</summary><pre style={{ overflow: "auto", maxHeight: 420 }}>{result.output}</pre></details>
        </div>}
      </div>
    </section>
  );
}
