import { useState } from "react";
import "./App.css";

type HealthResponse = {
  status: string;
  service: string;
};

type SimulationResult = {
  failedNode: string;
  severity: string;
  directlyAffected: string[];
  indirectlyAffected: string[];
  unaffected: string[];
  explanation: string;
};

function App() {
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [simulation, setSimulation] = useState<SimulationResult | null>(null);
  const [error, setError] = useState<string>("");

  async function checkBackendHealth() {
    try {
      setError("");

      const response = await fetch("http://localhost:8080/api/health");

      if (!response.ok) {
        throw new Error("Backend returned an error");
      }

      const data: HealthResponse = await response.json();
      setHealth(data);
    } catch (err) {
      setError("Could not connect to the backend.");
      setHealth(null);
    }
  }

  async function runSampleSimulation() {
    try {
      setError("");
      setSimulation(null);

      const response = await fetch(
        "http://localhost:8080/api/simulations/outage/sample"
      );

      if (!response.ok) {
        throw new Error("Simulation failed.");
      }

      const data: SimulationResult = await response.json();
      setSimulation(data);
    } catch (err) {
      setError("Could not run the outage simulation.");
      setSimulation(null);
    }
  }

  return (
    <main className="app">
      <section className="hero">
        <p className="eyebrow">Architecture Simulation Platform</p>

        <h1>SystemLens</h1>

        <p className="subtitle">
          Model software systems as dependency graphs and simulate outages,
          schema changes, and traffic spikes to understand downstream impact
          before production.
        </p>

        <div className="action-panel">
          <h2>Sample Outage Simulation</h2>
          <p>
            Run a sample simulation where the Payment Provider fails in an
            e-commerce system.
          </p>

          <button onClick={runSampleSimulation}>
            Run Payment Provider Outage
          </button>
        </div>

        {simulation && (
          <div className="result-card">
            <h2>Simulation Result</h2>

            <p>
              <strong>Failed Node:</strong> {simulation.failedNode}
            </p>

            <p>
              <strong>Severity:</strong> {simulation.severity}
            </p>

            <div>
              <strong>Directly Affected:</strong>
              <ul>
                {simulation.directlyAffected.map((node) => (
                  <li key={node}>{node}</li>
                ))}
              </ul>
            </div>

            <div>
              <strong>Indirectly Affected:</strong>
              <ul>
                {simulation.indirectlyAffected.map((node) => (
                  <li key={node}>{node}</li>
                ))}
              </ul>
            </div>

            <div>
              <strong>Unaffected:</strong>
              <ul>
                {simulation.unaffected.map((node) => (
                  <li key={node}>{node}</li>
                ))}
              </ul>
            </div>

            <p>{simulation.explanation}</p>
          </div>
        )}

        <div className="health-card">
          <h2>Backend Connection</h2>
          <p>
            Test whether the React frontend can reach the Spring Boot API.
          </p>

          <button onClick={checkBackendHealth}>Check API Health</button>

          {health && (
            <div className="success">
              <p>Status: {health.status}</p>
              <p>Service: {health.service}</p>
            </div>
          )}

          {error && <p className="error">{error}</p>}
        </div>
      </section>
    </main>
  );
}

export default App;