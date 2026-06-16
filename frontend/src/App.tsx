import { useEffect, useMemo, useState } from "react";
import {
  Background,
  Controls,
  MarkerType,
  ReactFlow,
  type Edge,
  type Node,
} from "@xyflow/react";
import "./App.css";

type HealthResponse = {
  status: string;
  service: string;
};

type SimulationMode = "outage" | "schema-change";

type SimulationResult = {
  failedNode: string;
  severity: string;
  directlyAffected: string[];
  indirectlyAffected: string[];
  unaffected: string[];
  impactPaths: string[][];
  explanation: string;
};

type ArchitectureGraphResponse = {
  nodes: ArchitectureNodeResponse[];
  edges: ArchitectureEdgeResponse[];
};

type ArchitectureNodeResponse = {
  id: string;
  label: string;
  type: string;
};

type ArchitectureEdgeResponse = {
  id: string;
  source: string;
  target: string;
  relationship: string;
};

const nodePositions: Record<string, { x: number; y: number }> = {
  "payment-provider": { x: 0, y: 220 },
  "order-database": { x: 0, y: 340 },
  "inventory-database": { x: 0, y: 460 },
  "email-queue": { x: 0, y: 580 },

  "cart-service": { x: 300, y: 120 },
  "checkout-service": { x: 300, y: 280 },
  "inventory-service": { x: 300, y: 440 },
  "notification-worker": { x: 300, y: 580 },

  "auth-service": { x: 600, y: 80 },
  "product-service": { x: 600, y: 220 },
  "web-app": { x: 900, y: 280 },
};

function getNodeImpactClass(
  label: string,
  simulation: SimulationResult | null
) {
  if (!simulation) {
    return "node-neutral";
  }

  if (label === simulation.failedNode) {
    return "node-failed";
  }

  if (simulation.directlyAffected.includes(label)) {
    return "node-direct";
  }

  if (simulation.indirectlyAffected.includes(label)) {
    return "node-indirect";
  }

  return "node-unaffected";
}

function App() {
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [nodes, setNodes] = useState<string[]>([]);
  const [selectedNode, setSelectedNode] = useState("Payment Provider");
  const [simulationMode, setSimulationMode] =
    useState<SimulationMode>("outage");
  const [simulation, setSimulation] = useState<SimulationResult | null>(null);
  const [graph, setGraph] = useState<ArchitectureGraphResponse | null>(null);
  const [error, setError] = useState<string>("");
  const [isSimulating, setIsSimulating] = useState(false);

  useEffect(() => {
    async function fetchInitialData() {
      try {
        const [nodesResponse, graphResponse] = await Promise.all([
          fetch("http://localhost:8080/api/simulations/sample/nodes"),
          fetch("http://localhost:8080/api/simulations/sample/graph"),
        ]);

        if (!nodesResponse.ok || !graphResponse.ok) {
          throw new Error("Could not fetch sample architecture.");
        }

        const nodesData: string[] = await nodesResponse.json();
        const graphData: ArchitectureGraphResponse = await graphResponse.json();

        setNodes(nodesData);
        setGraph(graphData);

        if (nodesData.length > 0) {
          setSelectedNode(nodesData[0]);
        }
      } catch (err) {
        setError("Could not load sample architecture.");
      }
    }

    fetchInitialData();
  }, []);

  const flowNodes: Node[] = useMemo(() => {
    if (!graph) {
      return [];
    }

    return graph.nodes.map((node) => ({
      id: node.id,
      position: nodePositions[node.id] ?? { x: 0, y: 0 },
      data: {
        label: (
          <div>
            <strong>{node.label}</strong>
            <span>{node.type}</span>
          </div>
        ),
      },
      className: getNodeImpactClass(node.label, simulation),
    }));
  }, [graph, simulation]);

  const flowEdges: Edge[] = useMemo(() => {
    if (!graph) {
      return [];
    }

    return graph.edges.map((edge) => ({
      id: edge.id,
      source: edge.source,
      target: edge.target,
      label: edge.relationship,
      markerEnd: {
        type: MarkerType.ArrowClosed,
      },
    }));
  }, [graph]);

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

  async function runSimulation() {
    try {
      setError("");
      setSimulation(null);
      setIsSimulating(true);

      const endpoint =
        simulationMode === "outage"
          ? "http://localhost:8080/api/simulations/outage"
          : "http://localhost:8080/api/simulations/schema-change";

      const requestBody =
        simulationMode === "outage"
          ? { failedNode: selectedNode }
          : { changedNode: selectedNode };

      const response = await fetch(endpoint, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(requestBody),
      });

      if (!response.ok) {
        throw new Error("Simulation failed.");
      }

      const data: SimulationResult = await response.json();
      setSimulation(data);
    } catch (err) {
      setError("Could not run the simulation.");
      setSimulation(null);
    } finally {
      setIsSimulating(false);
    }
  }

  const simulationTitle =
    simulationMode === "outage" ? "Outage Simulation" : "Schema Change Simulation";

  const selectedNodeLabel =
    simulationMode === "outage" ? "Failed node" : "Changed node";

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
          <h2>{simulationTitle}</h2>

          <p>
            Choose a system node and simulate how impact propagates through the
            architecture.
          </p>

          <div className="mode-toggle">
            <button
              className={
                simulationMode === "outage"
                  ? "mode-button active"
                  : "mode-button"
              }
              onClick={() => {
                setSimulationMode("outage");
                setSimulation(null);
              }}
            >
              Service Outage
            </button>

            <button
              className={
                simulationMode === "schema-change"
                  ? "mode-button active"
                  : "mode-button"
              }
              onClick={() => {
                setSimulationMode("schema-change");
                setSimulation(null);
              }}
            >
              Schema Change
            </button>
          </div>

          <div className="simulation-controls">
            <div>
              <label htmlFor="selected-node">{selectedNodeLabel}</label>

              <select
                id="selected-node"
                value={selectedNode}
                onChange={(event) => setSelectedNode(event.target.value)}
              >
                {nodes.map((node) => (
                  <option key={node} value={node}>
                    {node}
                  </option>
                ))}
              </select>
            </div>

            <button onClick={runSimulation} disabled={isSimulating}>
              {isSimulating ? "Running Simulation..." : `Run ${simulationTitle}`}
            </button>
          </div>
        </div>

        <div className="graph-card">
          <div className="graph-header">
            <div>
              <p className="eyebrow">System Graph</p>
              <h2>Sample E-commerce Architecture</h2>
            </div>

            <div className="legend">
              <span>
                <i className="legend-dot failed" />
                Source
              </span>
              <span>
                <i className="legend-dot direct-dot" />
                Direct
              </span>
              <span>
                <i className="legend-dot indirect-dot" />
                Indirect
              </span>
              <span>
                <i className="legend-dot unaffected-dot" />
                Unaffected
              </span>
            </div>
          </div>

          <div className="flow-wrapper">
            <ReactFlow nodes={flowNodes} edges={flowEdges} fitView>
              <Background />
              <Controls />
            </ReactFlow>
          </div>
        </div>

        {simulation && (
          <div className="result-card">
            <div className="result-header">
              <div>
                <p className="eyebrow">
                  {simulationMode === "outage"
                    ? "Outage Simulation Result"
                    : "Schema Change Simulation Result"}
                </p>

                <h2>
                  {simulation.failedNode}{" "}
                  {simulationMode === "outage" ? "Outage" : "Schema Change"}
                </h2>
              </div>

              <span className={`severity severity-${simulation.severity}`}>
                {simulation.severity}
              </span>
            </div>

            <p className="explanation">{simulation.explanation}</p>

            <div className="impact-paths">
              <h3>Impact Paths</h3>

              {simulation.impactPaths.length > 0 ? (
                <div className="path-list">
                  {simulation.impactPaths.map((path, index) => (
                    <div
                      className="impact-path"
                      key={`${path.join("-")}-${index}`}
                    >
                      {path.map((node, nodeIndex) => (
                        <span key={`${node}-${nodeIndex}`}>
                          <span
                            className={
                              nodeIndex === 0
                                ? "path-node failed-path-node"
                                : "path-node"
                            }
                          >
                            {node}
                          </span>

                          {nodeIndex < path.length - 1 && (
                            <span className="path-arrow">→</span>
                          )}
                        </span>
                      ))}
                    </div>
                  ))}
                </div>
              ) : (
                <p>No downstream impact paths found.</p>
              )}
            </div>

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