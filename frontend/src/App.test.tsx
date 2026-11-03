import {
  render,
  screen,
  fireEvent,
  waitFor,
  cleanup,
} from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import App from "./App";
const health = {
  backend: "UP",
  database: "UP",
  ai: {
    configured: false,
    status: "NOT_CONFIGURED",
    message: "Set NVIDIA_API_KEY",
    model: "meta/muse-glimmer-30b",
    provider: "nvidia",
  },
};
const run = {
  id: "00000000-0000-0000-0000-000000000001",
  scenario: "DUPLICATE_ORDER",
  createdAt: "2026-10-29T06:00:00Z",
  status: "FAILED",
  orders: [{ id: "one" }, { id: "two" }],
  events: [
    {
      id: "event-one",
      runId: "run-one",
      sequence: 1,
      timestamp: "2026-10-29T06:00:00Z",
      type: "ORDER_REQUESTED",
      component: "orchestrator",
      severity: "INFO",
      message: "A real recorded event",
      correlationId: "corr",
      idempotencyKey: "key",
      metadata: { attempt: "1" },
    },
  ],
};
beforeEach(() => {
  location.hash = "view=overview";
  vi.stubGlobal(
    "fetch",
    vi.fn(async (url: string, options?: RequestInit) => {
      let body: unknown = null;
      if (url.includes("/health")) body = health;
      else if (url.includes("/dashboard"))
        body = {
          totalRuns: 0,
          totalInvestigations: 0,
          failedSimulations: 0,
          duplicateOrders: 0,
          verifiedRuns: 0,
          verificationPassRate: -1,
        };
      else if (url === "/api/simulations?page=0&size=12")
        body = { content: [], totalElements: 0, page: 0, size: 12, last: true };
      else if (url === "/api/simulations/run" && options?.method === "POST")
        body = run;
      else if (
        url.includes("/investigations/") ||
        url.includes("/verification/")
      )
        return { ok: true, status: 204 };
      else body = run;
      return { ok: true, status: 200, json: async () => body };
    }),
  );
});
afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});
describe("workspace interactions", () => {
  it("refreshes persisted dashboard data when returning to overview", async () => {
    let recordedRuns = 0;
    const originalFetch = fetch;
    vi.stubGlobal(
      "fetch",
      vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
        if (String(input).endsWith("/dashboard"))
          return {
            ok: true,
            status: 200,
            json: async () => ({
              totalRuns: recordedRuns,
              totalInvestigations: 0,
              failedSimulations: 0,
              duplicateOrders: 0,
              verifiedRuns: 0,
              verificationPassRate: -1,
            }),
          } as Response;
        return originalFetch(input, init);
      }),
    );
    render(<App />);
    await screen.findByText("0 simulation runs recorded");
    recordedRuns = 3;
    fireEvent.click(screen.getByRole("button", { name: "Run history" }));
    await screen.findByRole("heading", { name: "Run history", level: 1 });
    fireEvent.click(screen.getByRole("button", { name: "Simulation lab" }));
    await screen.findByText("3 simulation runs recorded");
  });
  it("labels the NVIDIA host and its backend-only key accurately", async () => {
    const originalFetch = fetch;
    vi.stubGlobal(
      "fetch",
      vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
        if (String(input).endsWith("/health")) {
          return {
            ok: true,
            status: 200,
            json: async () => ({
              ...health,
              ai: {
                ...health.ai,
                provider: "nvidia",
                model: "meta/muse-glimmer-30b",
                configured: true,
              },
            }),
          } as Response;
        }
        return originalFetch(input, init);
      }),
    );
    render(<App />);
    await screen.findByText("NVIDIA configured");
    fireEvent.click(screen.getByRole("button", { name: "System status" }));
    expect(screen.queryByText("NVIDIA_API_KEY")).not.toBeInTheDocument();
    expect(screen.queryByText("META_API_KEY")).not.toBeInTheDocument();
    expect(screen.getByText("meta/muse-glimmer-30b")).toBeInTheDocument();
  });
  it("clears the connection error after a successful refresh", async () => {
    const originalFetch = fetch;
    const flaky = vi.fn().mockRejectedValue(new Error("offline"));
    vi.stubGlobal("fetch", flaky);
    render(<App />);
    await screen.findByRole("alert");
    flaky.mockImplementation(originalFetch);
    fireEvent.click(screen.getByRole("button", { name: "Retry" }));
    await waitFor(() =>
      expect(screen.queryByRole("alert")).not.toBeInTheDocument(),
    );
    expect(
      screen.getByRole("button", { name: /Start a simulation/ }),
    ).toBeEnabled();
  });
  it("retries a deep-linked run after the backend recovers", async () => {
    location.hash = "view=investigation&run=" + run.id;
    const originalFetch = fetch;
    const flaky = vi.fn().mockRejectedValue(new Error("offline"));
    vi.stubGlobal("fetch", flaky);
    render(<App />);
    await screen.findByRole("alert");
    flaky.mockImplementation(originalFetch);
    fireEvent.click(screen.getByRole("button", { name: "Retry" }));
    await screen.findByRole("heading", { name: "Execution timeline" });
    expect(
      screen.getByRole("region", { name: "Execution events" }),
    ).toHaveAttribute("tabindex", "0");
  });
  it("shows honest empty metrics", async () => {
    render(<App />);
    await screen.findByText("Your first trace starts here");
    expect(
      screen.getByText("Run verification to establish a baseline"),
    ).toBeInTheDocument();
    expect(screen.getByText("NVIDIA not configured")).toBeInTheDocument();
  });
  it("launches a simulation and inspects stored evidence", async () => {
    render(<App />);
    const start = screen.getByRole("button", { name: /Start a simulation/ });
    await waitFor(() => expect(start).toBeEnabled());
    fireEvent.click(start);
    expect(screen.getByRole("dialog")).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: "Run simulation" }));
    await screen.findByText("Investigation workspace");
    expect(screen.getAllByText("A real recorded event").length).toBeGreaterThan(
      0,
    );
    expect(
      screen.getByRole("button", { name: "Investigate trace" }),
    ).toBeDisabled();
    expect(fetch).toHaveBeenCalledWith(
      "/api/simulations/run",
      expect.objectContaining({
        method: "POST",
        body: '{"scenario":"DUPLICATE_ORDER"}',
      }),
    );
  });
  it("opens system status and keeps credentials on the backend", async () => {
    render(<App />);
    fireEvent.click(screen.getByRole("button", { name: "System status" }));
    await screen.findByText("NVIDIA setup required");
    expect(screen.getByText("Backend environment only")).toBeInTheDocument();
    expect(screen.queryByRole("textbox")).not.toBeInTheDocument();
  });
  it("reports backend failures without inventing data", async () => {
    vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new Error("offline")));
    render(<App />);
    await screen.findByRole("alert");
    expect(
      screen.getByRole("button", { name: /Start a simulation/ }),
    ).toBeDisabled();
    fireEvent.click(screen.getByRole("button", { name: "System status" }));
    await screen.findByText("Backend connection unavailable");
    expect(screen.queryByText("META_API_KEY")).not.toBeInTheDocument();
    expect(screen.queryByText("NVIDIA_API_KEY")).not.toBeInTheDocument();
  });
});
