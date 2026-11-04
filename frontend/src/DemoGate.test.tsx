import { afterEach, expect, it, vi } from "vitest";
import { act, cleanup, fireEvent, render, screen } from "@testing-library/react";
import DemoGate from "./DemoGate";
import { apiFetch, setDemoCredentials } from "./transport";
afterEach(() => { cleanup(); vi.unstubAllGlobals(); setDemoCredentials("", ""); });
it("asks for demo credentials and remounts the application after successful authentication", async () => {
  const network=vi.fn().mockResolvedValue({status:200,ok:true});
  vi.stubGlobal("fetch",network);
  render(<DemoGate><p>Application ready</p></DemoGate>);
  act(()=>window.dispatchEvent(new Event("traceback-sign-in")));
  fireEvent.change(screen.getByLabelText("Demo username"),{target:{value:"judge"}});
  fireEvent.change(screen.getByLabelText("Demo password"),{target:{value:"test-only-password"}});
  fireEvent.click(screen.getByRole("button",{name:"Sign in"}));
  expect(await screen.findByText("Application ready")).toBeInTheDocument();
  const headers=network.mock.calls[0][1].headers as Headers;
  expect(headers.get("Authorization")).toBe("Basic "+btoa("judge:test-only-password"));
  expect(localStorage.length).toBe(0);
});
it("shows a useful error for rejected credentials", async () => {
  vi.stubGlobal("fetch",vi.fn().mockResolvedValue({status:401,ok:false}));
  render(<DemoGate><p>Application ready</p></DemoGate>);
  act(()=>window.dispatchEvent(new Event("traceback-sign-in")));
  fireEvent.change(screen.getByLabelText("Demo username"),{target:{value:"judge"}});
  fireEvent.change(screen.getByLabelText("Demo password"),{target:{value:"wrong"}});
  fireEvent.click(screen.getByRole("button",{name:"Sign in"}));
  expect(await screen.findByRole("alert")).toHaveTextContent("Check your demo username");
});
it("keeps multipart headers intact and clears rejected credentials", async () => {
  const network=vi.fn().mockResolvedValueOnce({status:401}).mockResolvedValueOnce({status:200});
  vi.stubGlobal("fetch",network);setDemoCredentials("judge","test");
  await apiFetch("/api/projects",{method:"POST",body:new FormData()});
  await apiFetch("/api/projects");
  expect((network.mock.calls[0][1].headers as Headers).has("Content-Type")).toBe(false);
  expect((network.mock.calls[1][1].headers as Headers).has("Authorization")).toBe(false);
});
