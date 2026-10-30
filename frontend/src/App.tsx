import { useCallback, useEffect, useRef, useState } from "react";
import type { ReactNode } from "react";
import {
  Activity,
  ArrowRight,
  ArrowUpRight,
  Check,
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
  Copy,
  Database,
  FlaskConical,
  GitBranch,
  History,
  LayoutDashboard,
  Loader2,
  Play,
  Radio,
  RefreshCw,
  Search,
  ShieldCheck,
  Sparkles,
  Terminal,
  TriangleAlert,
  X,
  XCircle,
} from "lucide-react";
import { request, labels, descriptions, date, short } from "./api";
import type {
  Health,
  Investigation,
  Page,
  Run,
  Scenario,
  Stats,
  TraceEvent,
  Verification,
} from "./api";
type View =
  | "overview"
  | "investigation"
  | "verification"
  | "history"
  | "status";
const nav = [
  { id: "overview", label: "Overview", icon: LayoutDashboard },
  { id: "investigation", label: "Investigation", icon: GitBranch },
  { id: "verification", label: "Verification", icon: ShieldCheck },
  { id: "history", label: "Run history", icon: History },
  { id: "status", label: "System status", icon: Activity },
] as const;
function Badge({ value }: { value: string }) {
  const good = ["COMPLETED", "PASSED", "UP"].includes(value);
  const bad = ["FAILED", "ERROR", "DOWN"].includes(value);
  return (
    <span className={"badge " + (good ? "good" : bad ? "bad" : "neutral")}>
      <i />
      {value.replaceAll("_", " ").toLowerCase()}
    </span>
  );
}
function Empty({
  icon: Icon = GitBranch,
  title,
  children,
}: {
  icon?: typeof GitBranch;
  title: string;
  children: ReactNode;
}) {
  return (
    <div className="empty">
      <span className="empty-icon">
        <Icon size={26} />
      </span>
      <h3>{title}</h3>
      <div>{children}</div>
    </div>
  );
}
function CopyId({ id }: { id: string }) {
  const [copied, setCopied] = useState(false);
  return (
    <button
      className="copy"
      title="Copy full identifier"
      aria-label="Copy full identifier"
      onClick={async () => {
        try {
          await navigator.clipboard.writeText(id);
          setCopied(true);
          setTimeout(() => setCopied(false), 1600);
        } catch {
          setCopied(false);
        }
      }}
    >
      <code>{short(id)}</code>
      {copied ? <Check size={12} /> : <Copy size={12} />}
    </button>
  );
}
export default function App() {
  const initial = new URLSearchParams(location.hash.slice(1));
  const [view, setView] = useState<View>(
    (nav.some((n) => n.id === initial.get("view"))
      ? initial.get("view")
      : "overview") as View,
  );
  const [run, setRun] = useState<Run | null>(null),
    [event, setEvent] = useState<TraceEvent | null>(null),
    [investigation, setInvestigation] = useState<Investigation | null>(null),
    [verification, setVerification] = useState<Verification | null>(null);
  const [health, setHealth] = useState<Health | null>(null),
    [stats, setStats] = useState<Stats | null>(null),
    [history, setHistory] = useState<Page | null>(null);
  const ai = health?.ai ?? health?.meta;
  const providerLabel =
    ai?.provider === "nvidia"
      ? "NVIDIA"
      : ai?.provider === "meta"
        ? "Meta"
        : "AI";
  const keyVariable =
    ai?.provider === "nvidia" ? "NVIDIA_API_KEY" : "META_API_KEY";
  const [page, setPage] = useState(0),
    [query, setQuery] = useState(""),
    [scenario, setScenario] = useState<Scenario>("DUPLICATE_ORDER"),
    [modal, setModal] = useState(false);
  const [busy, setBusy] = useState(""),
    [error, setError] = useState(""),
    [notice, setNotice] = useState("");
  const generation = useRef(0);
  const launchButton = useRef<HTMLButtonElement>(null);
  const modalRef = useRef<HTMLDivElement>(null);
  const refresh = useCallback(async () => {
    const results = await Promise.allSettled([
      request<Health>("/health"),
      request<Stats>("/dashboard"),
      request<Page>(`/simulations?page=${page}&size=12`),
    ]);
    const [h, s, l] = results;
    if (h.status === "fulfilled") setHealth(h.value);
    else setHealth(null);
    if (s.status === "fulfilled") setStats(s.value);
    if (l.status === "fulfilled") setHistory(l.value);
    if (results.some((r) => r.status === "rejected"))
      setError(
        "Could not refresh all workspace data. Check the backend and retry.",
      );
  }, [page]);
  const navigate = useCallback((next: View, id?: string) => {
    setView(next);
    location.hash = new URLSearchParams({
      view: next,
      ...(id ? { run: id } : {}),
    }).toString();
  }, []);
  const loadRun = useCallback(
    async (id: string, next: View = "investigation") => {
      const token = ++generation.current;
      setBusy("Loading run");
      setError("");
      try {
        const [r, i, v] = await Promise.all([
          request<Run>(`/simulations/${id}`),
          request<Investigation>(`/investigations/${id}`),
          request<Verification>(`/verification/${id}`),
        ]);
        if (token !== generation.current) return;
        setRun(r);
        setEvent(r?.events[0] ?? null);
        setInvestigation(i);
        setVerification(v);
        navigate(next, id);
      } catch (e) {
        setError((e as Error).message);
      } finally {
        if (token === generation.current) setBusy("");
      }
    },
    [navigate],
  );
  useEffect(() => {
    void refresh();
  }, [refresh, view]);
  useEffect(() => {
    window.scrollTo({ top: 0, behavior: "instant" });
  }, [view]);
  useEffect(() => {
    const handle = () => {
      const p = new URLSearchParams(location.hash.slice(1));
      const v = p.get("view") as View;
      if (nav.some((n) => n.id === v)) setView(v);
      const id = p.get("run");
      if (id && id !== run?.id) void loadRun(id, v || "investigation");
    };
    window.addEventListener("hashchange", handle);
    return () => window.removeEventListener("hashchange", handle);
  }, [loadRun, run?.id]);
  useEffect(() => {
    const p = new URLSearchParams(location.hash.slice(1));
    if (p.get("run"))
      void loadRun(p.get("run")!, (p.get("view") as View) || "investigation");
  }, [loadRun]);
  useEffect(() => {
    if (!modal) return;
    const first = modalRef.current?.querySelector<HTMLElement>("button");
    first?.focus();
    const handle = (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        setModal(false);
        launchButton.current?.focus();
      }
      if (e.key === "Tab") {
        const items =
          modalRef.current?.querySelectorAll<HTMLElement>("button,select");
        if (!items?.length) return;
        const first = items[0],
          last = items[items.length - 1];
        if (e.shiftKey && document.activeElement === first) {
          e.preventDefault();
          last.focus();
        } else if (!e.shiftKey && document.activeElement === last) {
          e.preventDefault();
          first.focus();
        }
      }
    };
    document.addEventListener("keydown", handle);
    return () => document.removeEventListener("keydown", handle);
  }, [modal]);
  const action = async (name: string, fn: () => Promise<void>) => {
    setBusy(name);
    setError("");
    setNotice("");
    try {
      await fn();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy("");
    }
  };
  const launch = () =>
    action("Running simulation", async () => {
      const r = await request<Run>("/simulations/run", "POST", { scenario });
      if (!r) throw new Error("The backend returned no run.");
      setRun(r);
      setEvent(r.events[0]);
      setInvestigation(null);
      setVerification(null);
      setModal(false);
      navigate("investigation", r.id);
      setNotice(
        "Simulation recorded. Select an event to inspect its evidence.",
      );
      await refresh();
    });
  const investigate = () =>
    run &&
    action("Investigating trace", async () => {
      setInvestigation(
        await request<Investigation>(`/investigations/${run.id}`, "POST"),
      );
      setNotice("AI analysis saved. Evidence references have been validated.");
      await refresh();
    });
  const verify = () =>
    run &&
    action("Running verification", async () => {
      const v = await request<Verification>(`/verification/${run.id}`, "POST");
      setVerification(v);
      navigate("verification", run.id);
      setNotice(
        v?.status === "PASSED"
          ? "Executable verification passed. Review each measured check."
          : "Verification did not pass. Review the check results.",
      );
      await refresh();
    });
  const selectEvidence = (id: string) => {
    const target = run?.events.find((e) => e.id === id);
    if (target) {
      setEvent(target);
      document.getElementById(`event-${id}`)?.scrollIntoView({
        behavior: matchMedia("(prefers-reduced-motion: reduce)").matches
          ? "instant"
          : "smooth",
        block: "nearest",
      });
    }
  };
  const rows =
    history?.content.filter((r) =>
      `${labels[r.scenario]} ${r.id} ${r.status}`
        .toLowerCase()
        .includes(query.toLowerCase()),
    ) ?? [];
  const title = nav.find((n) => n.id === view)?.label;
  const header = (
    <div className="page-heading">
      <div>
        <div className="eyebrow">
          TRACEBACK <span>/</span> WORKSPACE
        </div>
        <h1>
          {view === "overview"
            ? "Overview"
            : view === "investigation"
              ? "Investigation workspace"
              : view === "verification"
                ? "Verification"
                : view === "history"
                  ? "Run history"
                  : "System status"}
        </h1>
        <p>
          {view === "overview"
            ? "Reproduce the failure. Follow the evidence. Verify what works."
            : view === "investigation"
              ? "A complete record of what happened — and the evidence behind why."
              : view === "verification"
                ? "Executable checks, measured outcomes, and an untouched original trace."
                : view === "history"
                  ? "Reopen any recorded run and continue where you left off."
                  : "Service health and integration readiness. Credentials stay on the backend."}
        </p>
      </div>
      {(view === "investigation" || view === "verification") && run ? (
        <button
          className="secondary"
          disabled={!!busy}
          onClick={() => void loadRun(run.id, view)}
        >
          <RefreshCw size={14} />
          Refresh run
        </button>
      ) : (
        <span className="environment">
          <Radio size={13} /> Local development
        </span>
      )}
    </div>
  );
  const table = (compact = false) => (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th>SCENARIO / RUN</th>
            <th>WORKFLOW</th>
            <th>EVENTS</th>
            <th>VERIFICATION</th>
            <th>RECORDED</th>
            <th>
              <span className="sr-only">Open</span>
            </th>
          </tr>
        </thead>
        <tbody>
          {(compact ? rows.slice(0, 5) : rows).map((r) => (
            <tr key={r.id}>
              <td>
                <button
                  className="row-link"
                  disabled={!!busy}
                  onClick={() => void loadRun(r.id)}
                >
                  {labels[r.scenario]}
                </button>
                <code className="sub-id">{short(r.id)}</code>
              </td>
              <td>
                <Badge value={r.status} />
              </td>
              <td className="mono">{r.eventCount}</td>
              <td>
                <Badge value={r.verificationStatus} />
              </td>
              <td className="muted small">{date(r.createdAt)}</td>
              <td>
                <button
                  className="icon-button"
                  aria-label={`Open ${labels[r.scenario]} ${short(r.id)}`}
                  disabled={!!busy}
                  onClick={() => void loadRun(r.id)}
                >
                  <ArrowUpRight size={16} />
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
  return (
    <div className="app">
      <a className="skip-link" href="#main">
        Skip to content
      </a>
      <aside className="sidebar">
        <a
          className="brand"
          href="#view=overview"
          onClick={() => navigate("overview")}
        >
          <span className="brand-symbol">
            <GitBranch size={22} />
          </span>
          TRACEBACK
        </a>
        <div className="workspace-label">
          <span className="workspace-dot" /> Local workspace{" "}
          <span className="workspace-mode">DEV</span>
        </div>
        <div className="nav-caption">WORKSPACE</div>
        <nav aria-label="Main navigation">
          {nav.map((n) => (
            <button
              key={n.id}
              className={view === n.id ? "active" : ""}
              aria-current={view === n.id ? "page" : undefined}
              onClick={() => navigate(n.id, run?.id)}
            >
              <n.icon size={17} />
              {n.label}
              {n.id === "investigation" && run && <span className="nav-dot" />}
            </button>
          ))}
        </nav>
        <div className="sidebar-spacer" />
        <button className="sidebar-health" onClick={() => navigate("status")}>
          <i className={health ? "online" : "offline"} />
          <div>
            {health ? "Backend connected" : "Backend unavailable"}
            <small>
              {health ? "Persistent trace storage" : "Check system status"}
            </small>
          </div>
          <ChevronRight size={14} />
        </button>
      </aside>
      <div className="shell">
        <header className="topbar">
          <div className="breadcrumb">
            Workspace <ChevronRight size={12} /> <strong>{title}</strong>
          </div>
          <div className="top-actions">
            <span className="meta-indicator">
              <Sparkles size={13} />
              {investigation
                ? `${investigation.provider.toUpperCase()} response validated`
                : ai?.configured
                  ? `${providerLabel} configured`
                  : `${providerLabel} not configured`}
            </span>
            <span className="topbar-divider" />
            <span className="local-label">Local only</span>
          </div>
        </header>
        <main id="main">
          {header}
          {error && (
            <div className="alert error" role="alert">
              <TriangleAlert size={17} />
              <span>{error}</span>
              <button
                onClick={() => {
                  setError("");
                  void refresh();
                }}
              >
                Retry
              </button>
              <button aria-label="Dismiss error" onClick={() => setError("")}>
                <X size={15} />
              </button>
            </div>
          )}
          {notice && (
            <div className="alert success" role="status">
              <CheckCircle2 size={16} />
              {notice}
              <button
                aria-label="Dismiss notification"
                onClick={() => setNotice("")}
              >
                <X size={14} />
              </button>
            </div>
          )}
          {busy && (
            <div className="loading-bar" role="status">
              <Loader2 size={14} className="spin" />
              {busy}…
            </div>
          )}
          {view === "overview" && (
            <>
              <section className="hero">
                <div className="hero-title">
                  <span className="section-kicker">INCIDENT INTELLIGENCE</span>
                  <h2>
                    Understand the failure.
                    <br />
                    <em>Prove the fix.</em>
                  </h2>
                </div>
                <div className="hero-action">
                  <p>
                    A clear path from a reproducible failure
                    <br className="desktop-break" /> to evidence you can trust.
                  </p>
                  <button
                    ref={launchButton}
                    className="primary"
                    disabled={!!busy || !health}
                    onClick={() => setModal(true)}
                  >
                    <Play size={14} />
                    Start a simulation
                    <ArrowRight size={14} />
                  </button>
                  <span className="hero-caption">
                    Deterministic scenarios. No real payments.
                  </span>
                </div>
              </section>
              <section className="stats" aria-label="Workspace statistics">
                {[
                  {
                    label: "Investigations",
                    value: stats?.totalInvestigations,
                    icon: GitBranch,
                    note: stats
                      ? `${stats.totalRuns} simulation runs recorded`
                      : "Waiting for backend data",
                  },
                  {
                    label: "Failed simulations",
                    value: stats?.failedSimulations,
                    icon: TriangleAlert,
                    note: "Controlled failures reproduced",
                  },
                  {
                    label: "Duplicate orders",
                    value: stats?.duplicateOrders,
                    icon: Copy,
                    note: "Detected across recorded runs",
                  },
                  {
                    label: "Verification pass rate",
                    value: stats
                      ? stats.verificationPassRate < 0
                        ? "—"
                        : `${Math.round(stats.verificationPassRate)}%`
                      : undefined,
                    icon: ShieldCheck,
                    note: stats?.verifiedRuns
                      ? `${stats.verifiedRuns} runs verified`
                      : "Run verification to establish a baseline",
                  },
                ].map((s) => (
                  <div className="stat" key={s.label}>
                    <div>
                      <span>{s.label}</span>
                      <s.icon size={16} />
                    </div>
                    <strong>{s.value ?? "—"}</strong>
                    <small>{s.note}</small>
                  </div>
                ))}
              </section>
              <section className="panel">
                <div className="panel-heading">
                  <div>
                    <h2>
                      Recent activity{" "}
                      <span className="count">
                        {history?.totalElements ?? 0}
                      </span>
                    </h2>
                    <p>
                      Recorded simulations and their current verification state
                    </p>
                  </div>
                  <button
                    className="text-button"
                    onClick={() => navigate("history")}
                  >
                    View all runs
                    <ArrowRight size={14} />
                  </button>
                </div>
                {rows.length ? (
                  table(true)
                ) : (
                  <Empty title="Your first trace starts here">
                    <p>
                      Run a scenario to create a real execution timeline.
                      <br />
                      No sample incidents or invented metrics.
                    </p>
                    <button
                      className="secondary"
                      disabled={!health || !!busy}
                      onClick={() => setModal(true)}
                    >
                      <Play size={14} />
                      Create first run
                    </button>
                  </Empty>
                )}
              </section>
              <div className="overview-bottom">
                <div>
                  <Database size={16} />
                  <span>Every run is persisted locally.</span>
                </div>
                <div>
                  <ShieldCheck size={16} />
                  <span>Verification runs independently of AI.</span>
                </div>
                <div>
                  <FlaskConical size={16} />
                  <span>No real payments are processed.</span>
                </div>
              </div>
            </>
          )}
          {(view === "investigation" || view === "verification") && !run && (
            <section className="panel">
              <Empty title="Select a run to begin">
                <p>
                  Open a previous run or create a simulation to inspect its
                  evidence.
                </p>
                <button
                  className="primary"
                  disabled={!health || !!busy}
                  onClick={() => setModal(true)}
                >
                  <Play size={14} />
                  New simulation
                </button>
                <button
                  className="secondary"
                  onClick={() => navigate("history")}
                >
                  Browse history
                </button>
              </Empty>
            </section>
          )}
          {(view === "investigation" || view === "verification") && run && (
            <>
              <section className="run-header">
                <div className="run-icon">
                  <GitBranch size={24} />
                </div>
                <div className="run-title">
                  <div className="eyebrow">SIMULATION RUN</div>
                  <h2>{labels[run.scenario]}</h2>
                  <div className="run-meta">
                    <CopyId id={run.id} />
                    <span>{date(run.createdAt)}</span>
                    <span>{run.events.length} events</span>
                    <span>{run.orders.length} orders</span>
                  </div>
                </div>
                <div className="run-badges">
                  <Badge value={run.status} />
                  <span>AI: {investigation ? "analyzed" : "not run"}</span>
                  <span>
                    Verification:{" "}
                    <Badge value={verification?.status ?? "NOT_RUN"} />
                  </span>
                </div>
              </section>
              <div className="tabs">
                <button
                  className={view === "investigation" ? "selected" : ""}
                  onClick={() => navigate("investigation", run.id)}
                >
                  <GitBranch size={15} />
                  Execution & investigation
                </button>
                <button
                  className={view === "verification" ? "selected" : ""}
                  onClick={() => navigate("verification", run.id)}
                >
                  <ShieldCheck size={15} />
                  Verification{verification && <i className="online" />}
                </button>
                <span className="tabs-end">ORIGINAL TRACE PRESERVED</span>
              </div>
            </>
          )}
          {view === "investigation" && run && (
            <div className="investigation-grid">
              <section className="panel timeline-panel">
                <div className="panel-heading">
                  <div>
                    <h2>Execution timeline</h2>
                    <p>Chronological, deterministic event sequence</p>
                  </div>
                  <span className="count mono">{run.events.length} EVENTS</span>
                </div>
                <div className="timeline">
                  {run.events.map((e) => (
                    <button
                      id={`event-${e.id}`}
                      key={e.id}
                      aria-pressed={event?.id === e.id}
                      className={`event ${event?.id === e.id ? "selected" : ""} ${e.severity.toLowerCase()}`}
                      onClick={() => setEvent(e)}
                    >
                      <span className="event-track">
                        <span className="event-node">
                          {e.severity === "ERROR" ? (
                            <X size={12} />
                          ) : e.severity === "WARN" ? (
                            <TriangleAlert size={11} />
                          ) : (
                            <Check size={11} />
                          )}
                        </span>
                      </span>
                      <span className="event-content">
                        <span className="event-label">
                          {e.type.replaceAll("_", " ").toLowerCase()}
                          <code>+{e.sequence * 100}ms</code>
                        </span>
                        <span className="event-message">{e.message}</span>
                        <span className="event-component">{e.component}</span>
                      </span>
                      <ChevronRight size={13} />
                    </button>
                  ))}
                </div>
                <div className="timeline-footer">
                  <span>TRACE COMPLETE</span>
                  <span>{run.events.length * 100}ms logical duration</span>
                </div>
              </section>
              <div className="insight-column">
                <section className="panel inspector">
                  <div className="panel-heading">
                    <h2>
                      <Search size={15} />
                      Event inspector
                    </h2>
                    {event && <Badge value={event.severity} />}
                  </div>
                  {event && (
                    <div className="panel-body">
                      <h3>{event.type.replaceAll("_", " ")}</h3>
                      <p>{event.message}</p>
                      <details className="inspector-details">
                        <summary>Event details</summary>
                        <dl>
                          <dt>Event ID</dt>
                          <dd>
                            <CopyId id={event.id} />
                          </dd>
                          <dt>Timestamp</dt>
                          <dd>{new Date(event.timestamp).toISOString()}</dd>
                          <dt>Component</dt>
                          <dd>{event.component}</dd>
                          <dt>Correlation ID</dt>
                          <dd>
                            <code>{event.correlationId}</code>
                          </dd>
                          <dt>Idempotency key</dt>
                          <dd>
                            <code>{event.idempotencyKey}</code>
                          </dd>
                        </dl>
                      </details>
                      <details>
                        <summary>Structured metadata</summary>
                        <pre>{JSON.stringify(event.metadata, null, 2)}</pre>
                      </details>
                    </div>
                  )}
                </section>
                <section className="panel ai-panel">
                  <div className="panel-heading">
                    <div>
                      <h2>
                        <Sparkles size={16} />
                        AI investigation
                      </h2>
                      <p>Hypotheses grounded in this run’s evidence</p>
                    </div>
                    <span className="ai-tag">
                      {(
                        investigation?.provider ??
                        ai?.provider ??
                        "AI"
                      ).toUpperCase()}
                    </span>
                  </div>
                  {investigation ? (
                    <div className="panel-body analysis">
                      <span className="section-kicker">
                        AI-GENERATED · {investigation.model}
                      </span>
                      <div className="hypothesis-block">
                        <h3>Root-cause hypothesis</h3>
                        <p className="hypothesis">
                          {investigation.analysis.rootCauseHypothesis}
                        </p>
                      </div>
                      <h4>Linked evidence</h4>
                      <div className="evidence-list">
                        {investigation.analysis.evidenceEventIds.map((id) => (
                          <button key={id} onClick={() => selectEvidence(id)}>
                            <GitBranch size={12} />
                            Event{" "}
                            {run.events.find((e) => e.id === id)?.sequence}
                            <ArrowUpRight size={12} />
                          </button>
                        ))}
                      </div>
                      <div className="recommendation-block">
                        <h4>Recommended correction</h4>
                        <p>{investigation.analysis.recommendedCorrection}</p>
                      </div>
                      <details className="analysis-disclosure">
                        <summary>Evidence & causal explanation</summary>
                        <h4>Direct evidence</h4>
                        <p>{investigation.analysis.directEvidence}</p>
                        <h4>Causal explanation</h4>
                        <p>{investigation.analysis.causalExplanation}</p>
                        <h4>Inference</h4>
                        <p>{investigation.analysis.inference}</p>
                      </details>
                      <details className="analysis-disclosure">
                        <summary>Verification plan</summary>
                        <p>{investigation.analysis.verificationPlan}</p>
                      </details>
                      <div className="uncertainty-note">
                        <span>Uncertainty</span>
                        <p>{investigation.analysis.uncertainty}</p>
                      </div>
                      {(
                        [
                          "alternativeExplanations",
                          "missingInformation",
                          "risksAndAssumptions",
                        ] as const
                      ).map((field) => (
                        <details key={field}>
                          <summary>{field.replace(/([A-Z])/g, " $1")}</summary>
                          {investigation.analysis[field].length ? (
                            <ul>
                              {investigation.analysis[field].map((s, i) => (
                                <li key={i}>{s}</li>
                              ))}
                            </ul>
                          ) : (
                            <p>None reported by the model.</p>
                          )}
                        </details>
                      ))}
                      <small>
                        AI analysis does not apply a correction or establish a
                        passing result.
                      </small>
                    </div>
                  ) : (
                    <div className="panel-body">
                      <div className="ai-empty">
                        <Sparkles size={27} />
                        <h3>Evidence before inference.</h3>
                        <p>
                          {ai?.configured
                            ? `Ask ${providerLabel} to examine this stored trace. This sends one bounded request to the configured model.`
                            : `${providerLabel} is not configured. Add the backend API key to enable evidence-backed analysis.`}
                        </p>
                      </div>
                      <button
                        className="primary wide"
                        disabled={!!busy || !ai?.configured}
                        onClick={() => void investigate()}
                      >
                        <Sparkles size={15} />
                        {busy === "Investigating trace"
                          ? "Analyzing trace…"
                          : "Investigate trace"}
                      </button>
                      {!ai?.configured && (
                        <button
                          className="text-button centered"
                          onClick={() => navigate("status")}
                        >
                          View integration status
                          <ArrowRight size={13} />
                        </button>
                      )}
                    </div>
                  )}
                </section>
                <section className="verification-callout">
                  <ShieldCheck size={22} />
                  <div>
                    <h3>Put the correction to the test.</h3>
                    <p>
                      Replay with idempotency protection and compare actual
                      outcomes.
                    </p>
                    <button
                      className="secondary"
                      disabled={!!busy}
                      onClick={() => void verify()}
                    >
                      <Play size={13} />
                      {verification
                        ? "Run verification again"
                        : "Run verification"}
                    </button>
                  </div>
                </section>
              </div>
            </div>
          )}
          {view === "verification" && run && (
            <>
              <section className="panel verification-intro">
                <div>
                  <span className="section-kicker">
                    DETERMINISTIC VERIFICATION
                  </span>
                  <h2>
                    {verification
                      ? verification.status === "PASSED"
                        ? "The checks passed. Here’s the evidence."
                        : "The checks found a failure."
                      : "A recommendation is a hypothesis. Test it."}
                  </h2>
                  <p>
                    {verification?.correction ??
                      (run.scenario === "DUPLICATE_ORDER"
                        ? "Replay the timeout-and-retry flow with idempotency protection. Compare the repaired execution to the preserved failure."
                        : "Check the recorded scenario against its expected order count, uniqueness and workflow outcome.")}
                  </p>
                </div>
                <button
                  className="primary"
                  disabled={!!busy}
                  onClick={() => void verify()}
                >
                  <ShieldCheck size={16} />
                  {busy === "Running verification"
                    ? "Running…"
                    : verification
                      ? "Run again"
                      : "Run verification"}
                </button>
              </section>
              {verification ? (
                <>
                  <div className="comparison">
                    <section className="panel">
                      <div className="panel-heading">
                        <h2>
                          Before{" "}
                          <span className="muted">Original execution</span>
                        </h2>
                        <Badge value={verification.before.workflowStatus} />
                      </div>
                      <div className="metric-row">
                        <div>
                          <span>Orders created</span>
                          <strong>{verification.before.ordersCreated}</strong>
                        </div>
                        <div>
                          <span>Logical orders</span>
                          <strong>
                            {verification.before.uniqueLogicalOrders}
                          </strong>
                        </div>
                        <div>
                          <span>Duplicates</span>
                          <strong
                            className={
                              verification.before.duplicateCount ? "coral" : ""
                            }
                          >
                            {verification.before.duplicateCount}
                          </strong>
                        </div>
                      </div>
                    </section>
                    <span className="comparison-arrow">
                      <ArrowRight size={22} />
                    </span>
                    <section className="panel after">
                      <div className="panel-heading">
                        <h2>
                          After{" "}
                          <span className="muted">
                            {verification.correctionApplied
                              ? "Protected replay"
                              : "Invariant check"}
                          </span>
                        </h2>
                        <Badge value={verification.after.workflowStatus} />
                      </div>
                      <div className="metric-row">
                        <div>
                          <span>Orders created</span>
                          <strong>{verification.after.ordersCreated}</strong>
                        </div>
                        <div>
                          <span>Logical orders</span>
                          <strong>
                            {verification.after.uniqueLogicalOrders}
                          </strong>
                        </div>
                        <div>
                          <span>Duplicates</span>
                          <strong className="mint">
                            {verification.after.duplicateCount}
                          </strong>
                        </div>
                      </div>
                    </section>
                  </div>
                  <section className="panel">
                    <div className="panel-heading">
                      <div>
                        <h2>Executable checks</h2>
                        <p>
                          Measured by Java · {date(verification.executedAt)}
                        </p>
                      </div>
                      <Badge value={verification.status} />
                    </div>
                    <div className="table-wrap">
                      <table>
                        <thead>
                          <tr>
                            <th>CHECK</th>
                            <th>EXPECTED</th>
                            <th>ACTUAL</th>
                            <th>RESULT</th>
                          </tr>
                        </thead>
                        <tbody>
                          {verification.checks.map((c) => (
                            <tr key={c.testName}>
                              <td>
                                <strong>{c.testName}</strong>
                                {c.failureExplanation && (
                                  <p className="coral">
                                    {c.failureExplanation}
                                  </p>
                                )}
                              </td>
                              <td className="mono">{c.expected}</td>
                              <td className="mono">{c.actual}</td>
                              <td>
                                <span
                                  className={c.passed ? "check-pass" : "coral"}
                                >
                                  {c.passed ? (
                                    <CheckCircle2 size={14} />
                                  ) : (
                                    <XCircle size={14} />
                                  )}{" "}
                                  {c.passed ? "Passed" : "Failed"}
                                </span>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </section>
                  {verification.repairedRun && (
                    <details className="panel replay">
                      <summary>
                        Inspect protected replay ·{" "}
                        {short(verification.repairedRun.id)}
                      </summary>
                      <div className="panel-body">
                        {verification.repairedRun.events.map((e) => (
                          <p key={e.id}>
                            <code>{String(e.sequence).padStart(2, "0")}</code>{" "}
                            <strong>{e.type.replaceAll("_", " ")}</strong> —{" "}
                            {e.message}
                          </p>
                        ))}
                      </div>
                    </details>
                  )}
                  <p className="footnote">
                    <ShieldCheck size={14} />
                    Passing verification confirms controlled simulator
                    invariants. External production code has not been modified.
                  </p>
                </>
              ) : (
                <section className="panel">
                  <Empty icon={ShieldCheck} title="Verification has not run">
                    <p>
                      Start the checks to see real before-and-after values.
                      <br />
                      AI configuration is not required.
                    </p>
                  </Empty>
                </section>
              )}
            </>
          )}
          {view === "history" && (
            <section className="panel">
              <div className="panel-heading">
                <div>
                  <h2>
                    Run history{" "}
                    <span className="count">{history?.totalElements ?? 0}</span>
                  </h2>
                  <p>
                    Persisted traces, investigations and verification results
                  </p>
                </div>
                <label className="search">
                  <Search size={14} />
                  <input
                    aria-label="Filter current history page"
                    placeholder="Filter this page…"
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                  />
                </label>
              </div>
              {rows.length ? (
                table()
              ) : (
                <Empty
                  icon={History}
                  title={
                    query
                      ? "No matching runs on this page"
                      : "No recorded runs yet"
                  }
                >
                  <p>
                    {query
                      ? "Try a different scenario, status or run identifier."
                      : "Create your first simulation from the overview."}
                  </p>
                </Empty>
              )}
              <div className="pagination">
                <span>
                  Page {page + 1} · {history?.totalElements ?? 0} total runs
                </span>
                <div>
                  <button
                    className="secondary"
                    disabled={page === 0 || !!busy}
                    onClick={() => setPage((p) => p - 1)}
                  >
                    <ChevronLeft size={14} />
                    Previous
                  </button>
                  <button
                    className="secondary"
                    disabled={!history || history.last || !!busy}
                    onClick={() => setPage((p) => p + 1)}
                  >
                    Next
                    <ChevronRight size={14} />
                  </button>
                </div>
              </div>
            </section>
          )}
          {view === "status" && (
            <div className="status-grid">
              <section className="panel">
                <div className="panel-heading">
                  <h2>Service health</h2>
                  <button
                    className="icon-button"
                    aria-label="Refresh system status"
                    onClick={() => void refresh()}
                  >
                    <RefreshCw size={15} />
                  </button>
                </div>
                <div className="service-row">
                  <span className="service-icon">
                    <Activity />
                  </span>
                  <div>
                    <h3>Spring Boot backend</h3>
                    <p>Local REST service · 127.0.0.1:8080</p>
                  </div>
                  <Badge value={health?.backend ?? "DOWN"} />
                </div>
                <div className="service-row">
                  <span className="service-icon">
                    <Database />
                  </span>
                  <div>
                    <h3>Persistent database</h3>
                    <p>Health checked with a database query</p>
                  </div>
                  <Badge value={health?.database ?? "NOT_VERIFIED"} />
                </div>
                <div className="panel-body">
                  <p className="muted">
                    The local profile uses a file database. The PostgreSQL
                    profile is available for deployment. Health does not imply
                    every database profile has been tested.
                  </p>
                </div>
              </section>
              <section className="panel ai-panel">
                <div className="panel-heading">
                  <h2>
                    <Sparkles size={16} />
                    {providerLabel} model API
                  </h2>
                  <Badge value={ai?.status ?? "NOT_VERIFIED"} />
                </div>
                <div className="panel-body">
                  <h3>
                    {ai?.configured
                      ? "Configured; authenticated access requires an investigation."
                      : "Ready when you configure it."}
                  </h3>
                  <p>
                    {ai?.message ??
                      "Connect the backend to read integration status."}
                  </p>
                  <dl>
                    <dt>Provider</dt>
                    <dd>{ai?.provider ?? "Unknown"}</dd>
                    <dt>Model</dt>
                    <dd>{ai?.model ?? "Unknown"}</dd>
                    <dt>Selected run analysis</dt>
                    <dd>
                      {investigation ? "Validated and stored" : "Not run"}
                    </dd>
                  </dl>
                  <div className="config-note">
                    <Terminal size={16} />
                    <div>
                      <strong>Backend environment only</strong>
                      <p>
                        Set <code>{keyVariable}</code>, restart the backend,
                        then explicitly run an investigation. Never place a key
                        in frontend settings.
                      </p>
                    </div>
                  </div>
                  <p className="small muted">
                    One bounded call per investigation. Saved analysis is
                    reused. Automated tests never spend model credits.
                  </p>
                </div>
              </section>
            </div>
          )}
          <footer>
            <span>
              TRACEBACK <span className="muted">/</span> Every failure has a
              cause.
            </span>
            <span>
              <i className={health ? "online" : "offline"} />
              {health
                ? "Connected to local backend"
                : "Backend connection required"}
            </span>
          </footer>
        </main>
      </div>
      {modal && (
        <div
          className="modal-backdrop"
          onClick={(e) => {
            if (e.target === e.currentTarget && !busy) setModal(false);
          }}
        >
          <div
            ref={modalRef}
            className="modal"
            role="dialog"
            aria-modal="true"
            aria-labelledby="simulation-title"
          >
            <div className="panel-heading">
              <div>
                <span className="section-kicker">CONTROLLED SIMULATION</span>
                <h2 id="simulation-title">Choose your failure path.</h2>
              </div>
              <button
                aria-label="Close simulation dialog"
                className="icon-button"
                disabled={!!busy}
                onClick={() => setModal(false)}
              >
                <X size={19} />
              </button>
            </div>
            <p className="muted">
              Every scenario generates a real, persisted trace. No real payment
              is made.
            </p>
            <div className="scenario-options">
              {(Object.keys(labels) as Scenario[]).map((s) => (
                <button
                  key={s}
                  className={scenario === s ? "chosen" : ""}
                  aria-pressed={scenario === s}
                  disabled={!!busy}
                  onClick={() => setScenario(s)}
                >
                  <span className="scenario-radio">
                    {scenario === s && <span />}
                  </span>
                  <span>
                    <strong>{labels[s]}</strong>
                    <small>{descriptions[s]}</small>
                  </span>
                  {s === "DUPLICATE_ORDER" && (
                    <span className="scenario-tag">START HERE</span>
                  )}
                </button>
              ))}
            </div>
            <div className="modal-footer">
              <span>
                <Database size={13} />
                Trace saved automatically
              </span>
              <button
                className="primary"
                disabled={!!busy}
                onClick={() => void launch()}
              >
                {busy ? (
                  <Loader2 size={15} className="spin" />
                ) : (
                  <Play size={15} />
                )}
                Run simulation
              </button>
            </div>
            {error && (
              <p className="coral" role="alert">
                {error}
              </p>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
