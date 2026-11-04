let authorization = "";
export function setDemoCredentials(username: string, password: string) {
  const bytes = new TextEncoder().encode(username + ":" + password);
  authorization = "Basic " + btoa(Array.from(bytes, b => String.fromCharCode(b)).join(""));
}
export async function apiFetch(url: string, init: RequestInit = {}) {
  const headers = new Headers(init.headers);
  if (authorization) headers.set("Authorization", authorization);
  const response = await fetch(url, { ...init, headers, cache: "no-store" });
  if (response.status === 401) {
    authorization = "";
    window.dispatchEvent(new Event("traceback-sign-in"));
  }
  return response;
}
