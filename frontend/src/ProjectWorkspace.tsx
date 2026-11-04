import { apiFetch } from "./transport";
import { useEffect, useState } from "react";
import {
  Upload,
  Play,
  RefreshCw,
  FileArchive,
  CheckCircle2,
  AlertCircle,
  Loader2,
} from "lucide-react";
import { request, date, short } from "./api";
interface Counts {
  tests: number;
  failures: number;
  errors: number;
  skipped: number;
}
interface Job {
  id: string;
  filename: string;
  createdAt: string;
  status: string;
  projectRoot: string;
  files: string[];
  command: string;
  output: string;
  counts: Counts | null;
  message: string;
}
interface Runner {
  available: boolean;
  image: string;
  message: string;
}
export default function ProjectWorkspace() {
  const [jobs, setJobs] = useState<Job[]>([]),
    [job, setJob] = useState<Job | null>(null),
    [runner, setRunner] = useState<Runner | null>(null);
  const [file, setFile] = useState<File | null>(null),
    [download, setDownload] = useState(false),
    [busy, setBusy] = useState(false),
    [error, setError] = useState("");
  const refresh = async () => {
    try {
      const [items, state] = await Promise.all([
        request<Job[]>("/projects"),
        request<Runner>("/projects/runner"),
      ]);
      setJobs(items ?? []);
      setJob((current) =>
        current
          ? (items?.find((item) => item.id === current.id) ?? current)
          : (items?.[0] ?? null),
      );
      setRunner(state);
      setError("");
    } catch (e) {
      setError((e as Error).message);
    }
  };
  useEffect(() => {
    let active = true;
    void Promise.all([
      request<Job[]>("/projects"),
      request<Runner>("/projects/runner"),
    ])
      .then(([items, state]) => {
        if (active) {
          setJobs(items ?? []);
          setRunner(state);
          setJob(items?.[0] ?? null);
        }
      })
      .catch((e) => {
        if (active) setError((e as Error).message);
      });
    return () => {
      active = false;
    };
  }, []);
  useEffect(() => {
    if (!job || job.status !== "RUNNING") return;
    const id = job.id;
    const timer = setInterval(() => {
      void request<Job>(`/projects/${id}`)
        .then((result) => {
          if (result) {
            setJob((current) => (current?.id === id ? result : current));
            if (result.status !== "RUNNING") void refresh();
          }
        })
        .catch((e) => setError((e as Error).message));
    }, 2000);
    return () => clearInterval(timer);
  }, [job]);
  const upload = async () => {
    if (!file) return;
    setBusy(true);
    setError("");
    try {
      const body = new FormData();
      body.append("file", file);
      const response = await apiFetch("/api/projects", {
        method: "POST",
        body,
        signal: AbortSignal.timeout(45000),
      });
      if (!response.ok) {
        const detail = await response.json();
        throw new Error(detail.message ?? "Upload failed.");
      }
      const result = (await response.json()) as Job;
      setJob(result);
      setDownload(false);
      await refresh();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  };
  const run = async () => {
    if (!job) return;
    setBusy(true);
    setError("");
    try {
      const result = await request<Job>(`/projects/${job.id}/test`, "POST", {
        downloadDependencies: download,
      });
      setJob(result);
      await refresh();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  };
  return (
    <div className="project-workspace">
      {error && (
        <div className="alert error" role="alert">
          <AlertCircle size={17} />
          <span>{error}</span>
          <button onClick={() => void refresh()}>Retry connection</button>
        </div>
      )}
      <section className="panel">
        <div className="panel-heading">
          <div>
            <h2>Test your Java project</h2>
            <p>
              Upload a Maven / Spring project ZIP. Run its real build and
              existing tests.
            </p>
          </div>
          <FileArchive size={21} />
        </div>
        <div className="panel-body project-upload">
          <label className="project-file">
            <Upload size={22} />
            <strong>Choose your project ZIP</strong>
            <span>
              Java 17 · Maven · up to 20 MB compressed / 100 MB extracted
            </span>
            <input
              aria-label="Java Maven project ZIP"
              type="file"
              accept=".zip,application/zip"
              disabled={busy}
              onChange={(e) => {
                setFile(e.target.files?.[0] ?? null);
                setError("");
              }}
            />
          </label>
          <button
            className="primary"
            disabled={!file || busy}
            onClick={() => void upload()}
          >
            {busy ? (
              <Loader2 size={15} className="spin" />
            ) : (
              <Upload size={15} />
            )}
            Upload project
          </button>
          <p className="muted small">
            Include pom.xml, source files and tests. Remove .env files and
            private keys. Uploading alone does not execute code.
          </p>
        </div>
      </section>
      <div className="project-runner">
        <span className={runner?.available ? "mint" : "muted"}>
          {runner?.message ?? "Checking Docker runner…"}
        </span>
        <button className="secondary" onClick={() => void refresh()}>
          <RefreshCw size={13} />
          Refresh
        </button>
      </div>
      <div className="project-columns">
        <section className="panel">
          <div className="panel-heading">
            <h2>Your uploads</h2>
            <span className="count">{jobs.length}</span>
          </div>
          <div className="project-list">
            {jobs.length ? (
              jobs.map((j) => (
                <button
                  key={j.id}
                  aria-pressed={job?.id === j.id}
                  onClick={() => {
                    setJob(j);
                    setDownload(false);
                  }}
                >
                  <FileArchive size={17} />
                  <span>
                    <strong>{j.filename}</strong>
                    <small>
                      {date(j.createdAt)} · {short(j.id)}
                    </small>
                  </span>
                  <span className="project-job-status">
                    {j.status.replaceAll("_", " ").toLowerCase()}
                  </span>
                </button>
              ))
            ) : (
              <p className="muted">Your uploaded projects will appear here.</p>
            )}
          </div>
        </section>
        <section className="panel">
          <div className="panel-heading">
            <h2>{job ? job.filename : "Project results"}</h2>
            {job && (
              <span className={job.status === "PASSED" ? "mint" : "muted"}>
                {job.status.replaceAll("_", " ")}
              </span>
            )}
          </div>
          <div className="panel-body">
            {!job ? (
              <p className="muted">
                Choose a ZIP above to see its files and run its tests.
              </p>
            ) : (
              <>
                <p role="status">{job.message}</p>
                <dl>
                  <dt>Build root</dt>
                  <dd>
                    <code>{job.projectRoot}/pom.xml</code>
                  </dd>
                  <dt>Files uploaded</dt>
                  <dd>{job.files.length}</dd>
                  <dt>Execution</dt>
                  <dd>
                    Separate Docker container · Java 17 · network disabled
                    during tests
                  </dd>
                </dl>
                <details>
                  <summary>
                    Files included in this upload ({job.files.length})
                  </summary>
                  <ul
                    className="project-files"
                    tabIndex={0}
                    aria-label="Uploaded file inventory"
                  >
                    {job.files.map((f) => (
                      <li key={f}>
                        <code>{f}</code>
                      </li>
                    ))}
                  </ul>
                </details>
                <div className="project-actions">
                  <label className="project-consent">
                    <input
                      type="checkbox"
                      checked={download}
                      disabled={job.status === "RUNNING"}
                      onChange={(e) => setDownload(e.target.checked)}
                    />
                    <span>
                      Allow a separate dependency-preparation container to
                      access the internet. Maven plugins may execute during this
                      step. No app credentials are passed.
                    </span>
                  </label>
                  <button
                    className="primary"
                    disabled={
                      busy || job.status === "RUNNING" || !runner?.available
                    }
                    onClick={() => void run()}
                  >
                    {job.status === "RUNNING" ? (
                      <Loader2 className="spin" size={16} />
                    ) : (
                      <Play size={16} />
                    )}{" "}
                    {job.status === "RUNNING"
                      ? "Running project tests…"
                      : "Run project tests"}
                  </button>
                </div>
                <p className="small muted">
                  The runner executes your project’s configured Maven tests. It
                  does not generate tests for every file. External databases and
                  services are unavailable during offline tests. An empty or
                  entirely skipped test suite is never marked passed.
                </p>
                {job.counts && (
                  <div className="project-counts">
                    {Object.entries(job.counts).map(([name, value]) => (
                      <div key={name}>
                        <span>{name}</span>
                        <strong>{value}</strong>
                      </div>
                    ))}
                  </div>
                )}
                {job.command && (
                  <p className="project-command">
                    <code>{job.command}</code>
                  </p>
                )}
                {job.status === "PASSED" && (
                  <p className="mint">
                    <CheckCircle2 size={15} /> Build and reported tests passed.
                  </p>
                )}
                {job.output && (
                  <details open>
                    <summary>Build and test output</summary>
                    <pre
                      className="project-output"
                      tabIndex={0}
                      aria-label="Maven build and test output"
                    >
                      {job.output}
                    </pre>
                  </details>
                )}
              </>
            )}
          </div>
        </section>
      </div>
    </div>
  );
}
