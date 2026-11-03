import {
  render,
  screen,
  fireEvent,
  waitFor,
  cleanup,
} from "@testing-library/react";
import { afterEach, beforeEach, it, expect, vi } from "vitest";
import ProjectWorkspace from "./ProjectWorkspace";
const job = {
  id: "abc",
  filename: "mine.zip",
  createdAt: "2026-11-03T09:00:00Z",
  status: "READY",
  projectRoot: "mine",
  files: ["mine/pom.xml", "mine/src/test/java/AppTest.java"],
  command: "",
  output: "",
  counts: null,
  message: "Project uploaded.",
};
beforeEach(() => {
  vi.stubGlobal(
    "fetch",
    vi.fn(async (input: string, init?: RequestInit) => {
      let body: unknown = input.endsWith("/runner")
        ? { available: true, message: "Docker ready", image: "runner" }
        : [];
      if (init?.method === "POST")
        body = input.endsWith("/test")
          ? {
              ...job,
              status: "PASSED",
              message: "Configured tests passed.",
              counts: { tests: 1, failures: 0, errors: 0, skipped: 0 },
              output: "Tests run: 1",
            }
          : job;
      return { ok: true, status: 200, json: async () => body };
    }),
  );
});
afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});
it("uploads the actual ZIP and displays its file inventory", async () => {
  render(<ProjectWorkspace />);
  await screen.findByText("Docker ready");
  const file = new File(["zip-fixture"], "mine.zip", {
    type: "application/zip",
  });
  fireEvent.change(screen.getByLabelText("Java Maven project ZIP"), {
    target: { files: [file] },
  });
  fireEvent.click(screen.getByRole("button", { name: "Upload project" }));
  await screen.findAllByText("mine/pom.xml");
  expect(
    screen.getByText("mine/src/test/java/AppTest.java"),
  ).toBeInTheDocument();
  expect(fetch).toHaveBeenCalledWith(
    "/api/projects",
    expect.objectContaining({ method: "POST", body: expect.any(FormData) }),
  );
});
it("runs only on explicit action and passes dependency consent", async () => {
  render(<ProjectWorkspace />);
  await screen.findByText("Docker ready");
  fireEvent.change(screen.getByLabelText("Java Maven project ZIP"), {
    target: { files: [new File(["zip"], "mine.zip")] },
  });
  fireEvent.click(screen.getByRole("button", { name: "Upload project" }));
  await screen.findByRole("button", { name: "Run project tests" });
  expect(
    vi.mocked(fetch).mock.calls.some((c) => String(c[0]).endsWith("/test")),
  ).toBe(false);
  fireEvent.click(screen.getByRole("checkbox"));
  fireEvent.click(screen.getByRole("button", { name: "Run project tests" }));
  await screen.findByText("Build and reported tests passed.");
  expect(fetch).toHaveBeenCalledWith(
    "/api/projects/abc/test",
    expect.objectContaining({ body: '{"downloadDependencies":true}' }),
  );
});
it("shows runner failures without claiming a result", async () => {
  vi.stubGlobal(
    "fetch",
    vi.fn().mockRejectedValue(new Error("Docker connection failed")),
  );
  render(<ProjectWorkspace />);
  await waitFor(() =>
    expect(screen.getByRole("alert")).toHaveTextContent(
      "Docker connection failed",
    ),
  );
  expect(
    screen.queryByText("Build and reported tests passed."),
  ).not.toBeInTheDocument();
});
