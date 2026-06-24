import type {
  CustomAuthFailureSimulationRequest,
  CustomOutageSimulationRequest,
  CustomSchemaChangeSimulationRequest,
  SimulationAnalysisResponse,
} from "../types";

const API_BASE_URL = "http://localhost:8080";

async function requestJson<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers ?? {}),
    },
    ...options,
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || `Request failed with status ${response.status}`);
  }

  return response.json() as Promise<T>;
}

export function runCustomOutageAnalysis(
  payload: CustomOutageSimulationRequest
): Promise<SimulationAnalysisResponse> {
  return requestJson<SimulationAnalysisResponse>(
    "/api/simulations/outage/custom/analyze",
    {
      method: "POST",
      body: JSON.stringify(payload),
    }
  );
}

export function runCustomSchemaChangeAnalysis(
  payload: CustomSchemaChangeSimulationRequest
): Promise<SimulationAnalysisResponse> {
  return requestJson<SimulationAnalysisResponse>(
    "/api/simulations/schema-change/custom/analyze",
    {
      method: "POST",
      body: JSON.stringify(payload),
    }
  );
}

export function runCustomAuthFailureAnalysis(
  payload: CustomAuthFailureSimulationRequest
): Promise<SimulationAnalysisResponse> {
  return requestJson<SimulationAnalysisResponse>(
    "/api/simulations/auth-failure/custom/analyze",
    {
      method: "POST",
      body: JSON.stringify(payload),
    }
  );
}