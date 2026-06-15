import { useEffect, useState } from "react";
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
  const [nodes, setNodes] = useState<string[]>([]);
  const [selectedNode, setSelectedNode] = useState("Payment Provider");
  const [simulation, setSimulation] = useState<SimulationResult | null>(null);
  const [error, setError] = useState<string>("");
  const [isSimulating, setIsSimulating] = useState(false);

  useEffect(() => {
    async function fetchNodes() {
      try {
        const response = await fetch(
          "http://localhost:8080/api/simulations/sample/nodes"
        );

        if (!response.ok) {
          throw new Error("Could not fetch nodes.");
        }

        const data: string[] = await response.json();
        setNodes(data);

        if (data.length > 0) {
          setSelectedNode(data[0]);
        }
      } catch (err) {
        setError("Could not load sample architecture nodes.");
      }
    }

    fetchNodes();
  }, []);

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

  async function runOutageSimulation() {
    try {
      setError("");
      setSimulation(null);
      setIsSimulating(true);

      const response = await fetch("http://localhost:8080/api/simulations/outage", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ failedNode: selectedNode }),
      });

      if (!response.ok) {
        throw new Error("Simulation failed.");
      }

      const data: SimulationResult = await response.json();
      setSimulation(data);
    } catch (err) {
      setError("Could not run the outage simulation.");
      setSimulation(null);
    } finally {
      setIsSimulating(false);
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
          <h2>Outage Simulation</h2>

          <p>
            Choose a system node and simulate what happens when it becomes
            unavailable.
          </p>

          <div className="simulation-controls">
            <label htmlFor="failed-node">Failed node</label>

            <select
              id="failed-node"
              value={selectedNode}
              onChange={(event) => setSelectedNode(event.target.value)}
            >
              {nodes.map((node) => (
                <option key={node} value={node}>
                  {node}
                </option>
              ))}
            </select>

            <button onClick={runOutageSimulation} disabled={isSimulating}>
              {isSimulating ? "Running Simulation..." : "Run Outage Simulation"}
            </button>
          </div>
        </div>

        {simulation && (
          <div className="result-card">
            <div className="result-header">
              <div>
                <p className="eyebrow">Simulation Result</p>
                <h2>{simulation.failedNode} Outage</h2>
              </div>

              <span className={`severity severity-${simulation.severity}`}>
                {simulation.severity}
              </span>
            </div>

            <p className="explanation">{simulation.explanation}</p>

            <div className="impact-grid">
              <div className="impact-column direct">
                <h3>Directly Affected</h3>
                {simulation.directlyAffected.length > 0 ? (
                  <ul>
                    {simulation.directlyAffected.map((node) => (
                      <li key={node}>{node}</li>
                    ))}
                  </ul>
                ) : (
                  <p>No direct downstream impact.</p>
                )}
              </div>

              <div className="impact-column indirect">
                <h3>Indirectly Affected</h3>
                {simulation.indirectlyAffected.length > 0 ? (
                  <ul>
                    {simulation.indirectlyAffected.map((node) => (
                      <li key={node}>{node}</li>
                    ))}
                  </ul>
                ) : (
                  <p>No indirect downstream impact.</p>
                )}
              </div>

              <div className="impact-column unaffected">
                <h3>Unaffected</h3>
                <ul>
                  {simulation.unaffected.map((node) => (
                    <li key={node}>{node}</li>
                  ))}
                </ul>
              </div>
            </div>
          </div>
        )}

        <div className="health-card">
          <h2>Backend Connection</h2>

          <p>Test whether the React frontend can reach the Spring Boot API.</p>

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
