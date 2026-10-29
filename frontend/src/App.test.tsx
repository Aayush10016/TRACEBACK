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
  meta: {
    configured: false,
    status: "NOT_CONFIGURED",
    message: "Set META_API_KEY",
    model: "muse-spark-1.3",
    provider: "meta",
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
  location.hash = "";
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
  it("shows honest empty metrics", async () => {
    render(<App />);
    await screen.findByText("Your first trace starts here");
    expect(
      screen.getByText("Run verification to establish a baseline"),
    ).toBeInTheDocument();
    expect(screen.getByText("Meta not configured")).toBeInTheDocument();
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
      screen.getByRole("button", { name: "Investigate with Meta" }),
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
    await screen.findByText("Ready when you configure it.");
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
  });
});
