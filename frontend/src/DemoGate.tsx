import { useEffect, useState, type ReactNode } from "react";
import { apiFetch, setDemoCredentials } from "./transport";
export default function DemoGate({children}: {children: ReactNode}) {
  const [required, setRequired] = useState(false);
  const [generation, setGeneration] = useState(0);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  useEffect(() => {
    const show = () => setRequired(true);
    window.addEventListener("traceback-sign-in", show);
    return () => window.removeEventListener("traceback-sign-in", show);
  }, []);
  if (!required) return <div key={generation}>{children}</div>;
  return <main className="demo-gate"><form className="demo-signin" onSubmit={async e => {
    e.preventDefault(); setBusy(true); setError("");
    try {
      setDemoCredentials(username, password);
      const result = await apiFetch("/api/projects/runner");
      if (result.status === 401) throw new Error("Check your demo username and password.");
      if (!result.ok && result.status !== 503) throw new Error("The backend is unavailable. Try again shortly.");
      setPassword(""); setGeneration(v => v + 1); setRequired(false);
    } catch (err) { setError((err as Error).message); }
    finally { setBusy(false); }
  }}>
    <span className="eyebrow">TRACEBACK</span>
    <h1>Welcome back.</h1><p>Sign in to the private demonstration.</p>
    <label htmlFor="demo-user">Demo username</label>
    <input id="demo-user" autoComplete="username" required value={username} onChange={e => setUsername(e.target.value)} />
    <label htmlFor="demo-password">Demo password</label>
    <input id="demo-password" type="password" autoComplete="current-password" required value={password} onChange={e => setPassword(e.target.value)} />
    {error && <p role="alert">{error}</p>}
    <button className="button primary" disabled={busy}>{busy ? "Signing in…" : "Sign in"}</button>
    <small>Use the demo credentials supplied by the owner.</small>
  </form></main>;
}
