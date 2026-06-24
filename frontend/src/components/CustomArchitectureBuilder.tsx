import { useEffect, useMemo, useState } from "react";
import {
  runCustomOutageAnalysis,
  runCustomSchemaChangeAnalysis,
} from "../api/systemLensApi";
import type {
  ArchitectureEdge,
  ArchitectureNode,
  SimulationAnalysisResponse,
} from "../types";
import "./CustomArchitectureBuilder.css";
import CustomArchitectureGraph from "./CustomArchitectureGraph";

const NODE_TYPES = [
  "frontend",
  "service",
  "database",
  "queue",
  "worker",
  "external",
];

const SAVED_ARCHITECTURE_KEY = "systemlens:saved-architecture";

type SavedArchitecture = {
  nodes: ArchitectureNode[];
  edges: ArchitectureEdge[];
  savedAt: string;
};

const starterNodes: ArchitectureNode[] = [
  {
    id: "user-database",
    label: "User Database",
    type: "database",
  },
  {
    id: "auth-service",
    label: "Auth Service",
    type: "service",
  },
  {
    id: "api-gateway",
    label: "API Gateway",
    type: "service",
  },
  {
    id: "web-app",
    label: "Web App",
    type: "frontend",
  },
  {
    id: "payment-provider",
    label: "Payment Provider",
    type: "external",
  },
  {
    id: "checkout-service",
    label: "Checkout Service",
    type: "service",
  },
  {
    id: "order-database",
    label: "Order Database",
    type: "database",
  },
];

const starterEdges: ArchitectureEdge[] = [
  {
    id: "edge-user-database-auth-service",
    sourceNode: "User Database",
    targetNode: "Auth Service",
    relationship: "supports",
  },
  {
    id: "edge-auth-service-api-gateway",
    sourceNode: "Auth Service",
    targetNode: "API Gateway",
    relationship: "supports",
  },
  {
    id: "edge-api-gateway-web-app",
    sourceNode: "API Gateway",
    targetNode: "Web App",
    relationship: "supports",
  },
  {
    id: "edge-payment-provider-checkout-service",
    sourceNode: "Payment Provider",
    targetNode: "Checkout Service",
    relationship: "supports",
  },
  {
    id: "edge-order-database-checkout-service",
    sourceNode: "Order Database",
    targetNode: "Checkout Service",
    relationship: "supports",
  },
  {
    id: "edge-checkout-service-web-app",
    sourceNode: "Checkout Service",
    targetNode: "Web App",
    relationship: "supports",
  },
];

function slugify(value: string): string {
  return value
    .trim()
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/(^-|-$)+/g, "");
}

function formatList(items: string[]): string {
  if (items.length === 0) {
    return "None";
  }

  return items.join(", ");
}

function formatSavedAt(savedAt: string): string {
  return new Intl.DateTimeFormat("en-US", {
    month: "short",
    day: "numeric",
    hour: "numeric",
    minute: "2-digit",
  }).format(new Date(savedAt));
}

function getDeepestPath(paths: string[][]): string {
  if (paths.length === 0) {
    return "No downstream path";
  }

  const deepestPath = paths.reduce((longest, current) =>
    current.length > longest.length ? current : longest
  );

  return deepestPath.join(" → ");
}

function isValidSavedArchitecture(value: unknown): value is SavedArchitecture {
  if (!value || typeof value !== "object") {
    return false;
  }

  const candidate = value as SavedArchitecture;

  return (
    Array.isArray(candidate.nodes) &&
    Array.isArray(candidate.edges) &&
    typeof candidate.savedAt === "string"
  );
}

export default function CustomArchitectureBuilder() {
  const [nodes, setNodes] = useState<ArchitectureNode[]>(starterNodes);
  const [edges, setEdges] = useState<ArchitectureEdge[]>(starterEdges);

  const [nodeLabel, setNodeLabel] = useState("");
  const [nodeType, setNodeType] = useState("service");

  const [sourceNode, setSourceNode] = useState(starterNodes[0].label);
  const [targetNode, setTargetNode] = useState(starterNodes[1].label);

  const [simulationType, setSimulationType] = useState<
    "outage" | "schema-change"
  >("outage");

  const [selectedSourceNode, setSelectedSourceNode] = useState(
    starterNodes[0].label
  );

  const [analysis, setAnalysis] = useState<SimulationAnalysisResponse | null>(
    null
  );
  const [error, setError] = useState("");
  const [statusMessage, setStatusMessage] = useState("");
  const [savedAt, setSavedAt] = useState<string | null>(null);
  const [isRunning, setIsRunning] = useState(false);

  const nodeLabels = useMemo(() => nodes.map((node) => node.label), [nodes]);

  const canRunSimulation = nodes.length > 0 && selectedSourceNode.trim() !== "";

  const totalImpactedSystems = analysis
    ? analysis.simulation.directlyAffected.length +
      analysis.simulation.indirectlyAffected.length
    : 0;

  useEffect(() => {
    const savedArchitecture = readSavedArchitecture();

    if (savedArchitecture) {
      setSavedAt(savedArchitecture.savedAt);
    }
  }, []);

  function resetResultState() {
    setAnalysis(null);
    setError("");
  }

  function setGraphState(
    updatedNodes: ArchitectureNode[],
    updatedEdges: ArchitectureEdge[]
  ) {
    setNodes(updatedNodes);
    setEdges(updatedEdges);

    const firstNode = updatedNodes[0]?.label ?? "";
    const secondNode = updatedNodes[1]?.label ?? firstNode;

    setSourceNode(firstNode);
    setTargetNode(secondNode);
    setSelectedSourceNode(firstNode);
  }

  function readSavedArchitecture(): SavedArchitecture | null {
    const rawSavedArchitecture = localStorage.getItem(SAVED_ARCHITECTURE_KEY);

    if (!rawSavedArchitecture) {
      return null;
    }

    try {
      const parsedArchitecture = JSON.parse(rawSavedArchitecture);

      if (!isValidSavedArchitecture(parsedArchitecture)) {
        return null;
      }

      return parsedArchitecture;
    } catch {
      return null;
    }
  }

  function saveArchitecture() {
    if (nodes.length === 0) {
      setError("Add at least one node before saving an architecture.");
      setStatusMessage("");
      return;
    }

    const savedArchitecture: SavedArchitecture = {
      nodes,
      edges,
      savedAt: new Date().toISOString(),
    };

    localStorage.setItem(
      SAVED_ARCHITECTURE_KEY,
      JSON.stringify(savedArchitecture)
    );

    setSavedAt(savedArchitecture.savedAt);
    setError("");
    setStatusMessage("Saved architecture locally.");
  }

  function loadSavedArchitecture() {
    const savedArchitecture = readSavedArchitecture();

    if (!savedArchitecture) {
      setError("No saved architecture found.");
      setStatusMessage("");
      return;
    }

    setGraphState(savedArchitecture.nodes, savedArchitecture.edges);
    setNodeLabel("");
    setNodeType("service");
    setSimulationType("outage");
    setSavedAt(savedArchitecture.savedAt);
    resetResultState();
    setStatusMessage("Loaded saved architecture.");
  }

  function clearSavedArchitecture() {
    localStorage.removeItem(SAVED_ARCHITECTURE_KEY);
    setSavedAt(null);
    setError("");
    setStatusMessage("Cleared saved architecture.");
  }

  function addNode() {
    const trimmedLabel = nodeLabel.trim();

    if (!trimmedLabel) {
      setError("Node label is required.");
      setStatusMessage("");
      return;
    }

    const labelExists = nodes.some(
      (node) => node.label.toLowerCase() === trimmedLabel.toLowerCase()
    );

    if (labelExists) {
      setError("A node with that label already exists.");
      setStatusMessage("");
      return;
    }

    const baseId = slugify(trimmedLabel);
    const id = baseId || `node-${nodes.length + 1}`;

    const newNode: ArchitectureNode = {
      id,
      label: trimmedLabel,
      type: nodeType,
    };

    const updatedNodes = [...nodes, newNode];

    setNodes(updatedNodes);
    setNodeLabel("");
    setSelectedSourceNode(trimmedLabel);

    if (updatedNodes.length === 1) {
      setSourceNode(trimmedLabel);
      setTargetNode(trimmedLabel);
    }

    resetResultState();
    setStatusMessage("");
  }

  function removeNode(label: string) {
    const updatedNodes = nodes.filter((node) => node.label !== label);
    const updatedEdges = edges.filter(
      (edge) => edge.sourceNode !== label && edge.targetNode !== label
    );

    setNodes(updatedNodes);
    setEdges(updatedEdges);

    const nextSelected = updatedNodes[0]?.label ?? "";

    if (selectedSourceNode === label) {
      setSelectedSourceNode(nextSelected);
    }

    if (sourceNode === label) {
      setSourceNode(nextSelected);
    }

    if (targetNode === label) {
      setTargetNode(updatedNodes[1]?.label ?? nextSelected);
    }

    resetResultState();
    setStatusMessage("");
  }

  function addEdge() {
    if (!sourceNode || !targetNode) {
      setError("Choose both a source and target node.");
      setStatusMessage("");
      return;
    }

    if (sourceNode === targetNode) {
      setError("A dependency cannot connect a node to itself.");
      setStatusMessage("");
      return;
    }

    const edgeExists = edges.some(
      (edge) => edge.sourceNode === sourceNode && edge.targetNode === targetNode
    );

    if (edgeExists) {
      setError("That dependency already exists.");
      setStatusMessage("");
      return;
    }

    const newEdge: ArchitectureEdge = {
      id: `edge-${slugify(sourceNode)}-${slugify(targetNode)}`,
      sourceNode,
      targetNode,
      relationship: "supports",
    };

    setEdges([...edges, newEdge]);
    resetResultState();
    setStatusMessage("");
  }

  function removeEdge(edgeId: string) {
    setEdges(edges.filter((edge) => edge.id !== edgeId));
    resetResultState();
    setStatusMessage("");
  }

  async function runSimulation() {
    if (!canRunSimulation) {
      setError("Choose a source node before running a simulation.");
      setStatusMessage("");
      return;
    }

    if (edges.length === 0) {
      setError("Add at least one dependency before running a simulation.");
      setStatusMessage("");
      return;
    }

    setIsRunning(true);
    setError("");
    setStatusMessage("");
    setAnalysis(null);

    try {
      const response =
        simulationType === "outage"
          ? await runCustomOutageAnalysis({
              nodes,
              edges,
              failedNode: selectedSourceNode,
            })
          : await runCustomSchemaChangeAnalysis({
              nodes,
              edges,
              changedNode: selectedSourceNode,
            });

      setAnalysis(response);
    } catch (caughtError) {
      const message =
        caughtError instanceof Error
          ? caughtError.message
          : "Something went wrong while running the simulation.";

      setError(message);
    } finally {
      setIsRunning(false);
    }
  }

  function loadStarterGraph() {
    setGraphState(starterNodes, starterEdges);
    setNodeLabel("");
    setNodeType("service");
    setSimulationType("outage");
    resetResultState();
    setStatusMessage("Loaded starter architecture.");
  }

  function clearGraph() {
    setNodes([]);
    setEdges([]);
    setNodeLabel("");
    setNodeType("service");
    setSourceNode("");
    setTargetNode("");
    setSelectedSourceNode("");
    resetResultState();
    setStatusMessage("Cleared current architecture.");
  }

  return (
    <section className="custom-builder">
      <div className="custom-builder__header">
        <div>
          <p className="custom-builder__eyebrow">Custom Architecture</p>
          <h2>Build a dependency graph and simulate its blast radius.</h2>
          <p>
            Add systems, connect dependencies, then run the same backend graph
            traversal engine against your custom architecture.
          </p>

          {savedAt && (
            <p className="saved-architecture-text">
              Saved locally: {formatSavedAt(savedAt)}
            </p>
          )}
        </div>

        <div className="custom-builder__actions">
          <button type="button" onClick={loadStarterGraph}>
            Load starter graph
          </button>

          <button type="button" onClick={saveArchitecture}>
            Save graph
          </button>

          <button type="button" onClick={loadSavedArchitecture}>
            Load saved graph
          </button>

          <button type="button" onClick={clearSavedArchitecture}>
            Clear saved graph
          </button>

          <button type="button" className="danger-button" onClick={clearGraph}>
            Clear
          </button>
        </div>
      </div>

      <div className="custom-builder__grid">
        <div className="builder-card">
          <h3>Add Node</h3>

          <label>
            Node label
            <input
              value={nodeLabel}
              onChange={(event) => setNodeLabel(event.target.value)}
              placeholder="Example: Checkout Service"
            />
          </label>

          <label>
            Node type
            <select
              value={nodeType}
              onChange={(event) => setNodeType(event.target.value)}
            >
              {NODE_TYPES.map((type) => (
                <option key={type} value={type}>
                  {type}
                </option>
              ))}
            </select>
          </label>

          <button type="button" onClick={addNode}>
            Add Node
          </button>
        </div>

        <div className="builder-card">
          <h3>Add Dependency</h3>

          <p className="helper-text">
            Direction matters: if the source fails or changes, the target may be
            affected.
          </p>

          <label>
            Source node
            <select
              value={sourceNode}
              onChange={(event) => setSourceNode(event.target.value)}
              disabled={nodes.length === 0}
            >
              {nodeLabels.map((label) => (
                <option key={label} value={label}>
                  {label}
                </option>
              ))}
            </select>
          </label>

          <label>
            Target node
            <select
              value={targetNode}
              onChange={(event) => setTargetNode(event.target.value)}
              disabled={nodes.length === 0}
            >
              {nodeLabels.map((label) => (
                <option key={label} value={label}>
                  {label}
                </option>
              ))}
            </select>
          </label>

          <button type="button" onClick={addEdge} disabled={nodes.length < 2}>
            Add Dependency
          </button>
        </div>

        <div className="builder-card">
          <h3>Run Simulation</h3>

          <div className="mode-toggle">
            <button
              type="button"
              className={simulationType === "outage" ? "active" : ""}
              onClick={() => {
                setSimulationType("outage");
                resetResultState();
              }}
            >
              Outage
            </button>

            <button
              type="button"
              className={simulationType === "schema-change" ? "active" : ""}
              onClick={() => {
                setSimulationType("schema-change");
                resetResultState();
              }}
            >
              Schema Change
            </button>
          </div>

          <label>
            Source node
            <select
              value={selectedSourceNode}
              onChange={(event) => {
                setSelectedSourceNode(event.target.value);
                resetResultState();
              }}
              disabled={nodes.length === 0}
            >
              {nodeLabels.map((label) => (
                <option key={label} value={label}>
                  {label}
                </option>
              ))}
            </select>
          </label>

          <button
            type="button"
            className="primary-button"
            onClick={runSimulation}
            disabled={!canRunSimulation || isRunning}
          >
            {isRunning ? "Running..." : "Run Simulation"}
          </button>
        </div>
      </div>

      {error && <div className="builder-error">{error}</div>}

      {statusMessage && <div className="builder-status">{statusMessage}</div>}

      <CustomArchitectureGraph
        nodes={nodes}
        edges={edges}
        simulation={analysis?.simulation ?? null}
      />

      {analysis && (
        <div className="blast-radius-card">
          <div>
            <p className="custom-builder__eyebrow">Blast Radius Summary</p>
            <h3>{totalImpactedSystems} downstream system(s) impacted</h3>
          </div>

          <div className="blast-radius-grid">
            <div>
              <span>Source</span>
              <strong>{analysis.simulation.failedNode}</strong>
            </div>

            <div>
              <span>Deepest path</span>
              <strong>{getDeepestPath(analysis.simulation.impactPaths)}</strong>
            </div>

            <div>
              <span>Risk</span>
              <strong>
                {analysis.riskAssessment.riskLevel} ·{" "}
                {analysis.riskAssessment.riskScore}/100
              </strong>
            </div>
          </div>
        </div>
      )}

      <div className="custom-builder__workspace">
        <div className="builder-card">
          <h3>Nodes</h3>

          {nodes.length === 0 ? (
            <p className="empty-state">No nodes yet.</p>
          ) : (
            <div className="node-list">
              {nodes.map((node) => (
                <div key={node.id} className="node-pill">
                  <div>
                    <strong>{node.label}</strong>
                    <span>{node.type}</span>
                  </div>
                  <button type="button" onClick={() => removeNode(node.label)}>
                    Remove
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="builder-card">
          <h3>Dependencies</h3>

          {edges.length === 0 ? (
            <p className="empty-state">No dependencies yet.</p>
          ) : (
            <div className="edge-list">
              {edges.map((edge) => (
                <div key={edge.id} className="edge-pill">
                  <span>
                    {edge.sourceNode} <strong>→</strong> {edge.targetNode}
                  </span>
                  <button type="button" onClick={() => removeEdge(edge.id)}>
                    Remove
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {analysis && (
        <div className="analysis-panel">
          <div className="analysis-panel__summary">
            <div>
              <p className="custom-builder__eyebrow">Simulation Result</p>
              <h3>
                {simulationType === "outage" ? "Outage" : "Schema Change"}{" "}
                Analysis
              </h3>
              <p>{analysis.simulation.explanation}</p>
            </div>

            <div className={`risk-badge ${analysis.riskAssessment.riskLevel}`}>
              <span>{analysis.riskAssessment.riskLevel}</span>
              <strong>{analysis.riskAssessment.riskScore}/100</strong>
            </div>
          </div>

          <div className="result-grid">
            <div className="result-card">
              <span>Directly affected</span>
              <strong>{analysis.simulation.directlyAffected.length}</strong>
              <p>{formatList(analysis.simulation.directlyAffected)}</p>
            </div>

            <div className="result-card">
              <span>Indirectly affected</span>
              <strong>{analysis.simulation.indirectlyAffected.length}</strong>
              <p>{formatList(analysis.simulation.indirectlyAffected)}</p>
            </div>

            <div className="result-card">
              <span>Unaffected</span>
              <strong>{analysis.simulation.unaffected.length}</strong>
              <p>{formatList(analysis.simulation.unaffected)}</p>
            </div>
          </div>

          <div className="analysis-section">
            <h4>Impact Paths</h4>
            {analysis.simulation.impactPaths.length === 0 ? (
              <p className="empty-state">No downstream impact paths found.</p>
            ) : (
              <ul>
                {analysis.simulation.impactPaths.map((path, index) => (
                  <li key={`${path.join("-")}-${index}`}>
                    {path.join(" → ")}
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="analysis-section">
            <h4>Risk Factors</h4>
            <ul>
              {analysis.riskAssessment.riskFactors.map((factor) => (
                <li key={factor}>{factor}</li>
              ))}
            </ul>
          </div>

          <div className="analysis-section">
            <h4>Recommendations</h4>
            <ul>
              {analysis.riskAssessment.recommendations.map((recommendation) => (
                <li key={recommendation}>{recommendation}</li>
              ))}
            </ul>
          </div>
        </div>
      )}
    </section>
  );
}