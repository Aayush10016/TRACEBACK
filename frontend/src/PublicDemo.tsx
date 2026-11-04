import { useState } from "react";
import { ArrowRight, GitBranch, ShieldCheck } from "lucide-react";

const steps = [
  {
    title: "Inspect the trace",
    body: "The order service commits the first order. The response times out before it reaches the client. An unsafe retry commits a second order with the same idempotency key.",
  },
  {
    title: "Read Glimmer’s analysis",
    body: "The first attempt committed successfully, but the timeout hid that result from the client. The retry was treated as a new creation because the order service did not enforce idempotency for the same logical order.",
  },
  {
    title: "Compare the replay",
    body: "With the idempotency check in place, the retry finds the first committed order and reuses it. The independent replay finished with one order and zero duplicates.",
  },
];

export default function PublicDemo() {
  const [selected, setSelected] = useState(0);
  return (
    <main className="public-demo">
      <header>
        <strong><GitBranch size={19} aria-hidden="true" /> TRACEBACK</strong>
        <a href="/?workspace=1">Open test workspace <ArrowRight size={16} aria-hidden="true" /></a>
      </header>
      <p className="public-demo-label">PUBLIC DEMO · SAVED SAMPLE RUN</p>
      <h1>Find the failure.<br />Verify the fix.</h1>
      <p>A customer submits one order. The request times out, the client retries, and the service creates two. Follow the evidence and see what changed when the incident was replayed.</p>
      <nav aria-label="Walkthrough steps">
        {steps.map((step, index) => (
          <button key={step.title} type="button" aria-pressed={selected === index} onClick={() => setSelected(index)}>
            {index + 1}. {step.title}
          </button>
        ))}
      </nav>
      <section aria-live="polite">
        <h2>{steps[selected].title}</h2>
        <p>{steps[selected].body}</p>
        {selected === 0 && <ol><li>First order committed</li><li>Response timed out</li><li>Retry committed a duplicate order</li></ol>}
        {selected === 1 && <p className="public-demo-detail">Saved Meta Muse Glimmer 30B analysis via NVIDIA. Evidence includes the first write, the timeout, and the second write.</p>}
        {selected === 2 && <p className="public-demo-detail"><ShieldCheck size={18} aria-hidden="true" /> Before: 2 orders, 1 duplicate. After: 1 order, 0 duplicates.</p>}
      </section>
      <p className="public-demo-detail">This walkthrough shows results from a completed sample. The test workspace lets authorized users run new investigations and upload Java 17 Maven ZIPs.</p>
    </main>
  );
}
