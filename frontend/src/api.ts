import { apiFetch } from "./transport";
export type Scenario =
  | "NORMAL_ORDER"
  | "DUPLICATE_ORDER"
  | "PAYMENT_FAILURE"
  | "TIMEOUT_WITH_IDEMPOTENCY";
export interface TraceEvent {
  id: string;
  runId: string;
  sequence: number;
  timestamp: string;
  type: string;
  component: string;
  severity: string;
  message: string;
  metadata: Record<string, string>;
  correlationId: string;
  idempotencyKey: string;
}
export interface Order {
  id: string;
  logicalOrderId: string;
  idempotencyKey: string;
  status: string;
}
export interface Run {
  id: string;
  scenario: Scenario;
  createdAt: string;
  status: string;
  events: TraceEvent[];
  orders: Order[];
}
export interface Summary {
  id: string;
  scenario: Scenario;
  createdAt: string;
  status: string;
  eventCount: number;
  orderCount: number;
  duplicateCount: number;
  investigationStatus: string;
  verificationStatus: string;
}
export interface Page {
  content: Summary[];
  totalElements: number;
  page: number;
  size: number;
  last: boolean;
}
export interface Metrics {
  ordersCreated: number;
  uniqueLogicalOrders: number;
  duplicateCount: number;
  workflowStatus: string;
}
export interface Verification {
  id: string;
  runId: string;
  executedAt: string;
  status: string;
  correctionApplied: boolean;
  correction: string;
  before: Metrics;
  after: Metrics;
  checks: {
    testName: string;
    expected: string;
    actual: string;
    passed: boolean;
    failureExplanation: string | null;
  }[];
  repairedRun: Run | null;
}
export interface Analysis {
  rootCauseHypothesis: string;
  causalExplanation: string;
  evidenceEventIds: string[];
  directEvidence: string;
  inference: string;
  alternativeExplanations: string[];
  missingInformation: string[];
  uncertainty: string;
  recommendedCorrection: string;
  verificationPlan: string;
  risksAndAssumptions: string[];
}
export interface Investigation {
  id: string;
  runId: string;
  createdAt: string;
  status: string;
  provider: string;
  model: string;
  analysis: Analysis;
}
export interface Health {
  backend: string;
  database: string;
  ai?: {
    configured: boolean;
    status: string;
    message: string;
    model: string;
    provider: string;
  };
  meta: {
    configured: boolean;
    status: string;
    message: string;
    model: string;
    provider: string;
  };
}
export interface Stats {
  totalRuns: number;
  totalInvestigations: number;
  failedSimulations: number;
  duplicateOrders: number;
  verifiedRuns: number;
  verificationPassRate: number;
}
export async function request<T>(
  path: string,
  method = "GET",
  body?: unknown,
): Promise<T | null> {
  const response = await apiFetch("/api" + path, {
    method,
    headers: body ? { "Content-Type": "application/json" } : undefined,
    body: body ? JSON.stringify(body) : undefined,
    signal: AbortSignal.timeout(
      path.startsWith("/investigations/") && method === "POST" ? 100000 : 45000,
    ),
  });
  if (!response.ok) {
    let message = "The service could not complete this request.";
    try {
      message = (await response.json()).message || message;
    } catch {
      /* A proxy may return a non-JSON error. */
    }
    throw new Error(message);
  }
  return response.status === 204 ? null : response.json();
}
export const labels: Record<Scenario, string> = {
  NORMAL_ORDER: "Normal order",
  DUPLICATE_ORDER: "Duplicate order",
  PAYMENT_FAILURE: "Payment failure",
  TIMEOUT_WITH_IDEMPOTENCY: "Timeout with idempotency",
};
export const descriptions: Record<Scenario, string> = {
  NORMAL_ORDER: "A clean payment and a single committed order.",
  DUPLICATE_ORDER: "A lost response. An unsafe retry. Two orders.",
  PAYMENT_FAILURE: "A declined payment stops the workflow safely.",
  TIMEOUT_WITH_IDEMPOTENCY: "The same retry, protected by a stable key.",
};
export const date = (value: string) =>
  new Date(value).toLocaleString(undefined, {
    month: "short",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });
export const short = (id: string) => id.slice(0, 8);
