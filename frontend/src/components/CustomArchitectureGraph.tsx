import { useMemo } from "react";
import {
  Background,
  Controls,
  MarkerType,
  ReactFlow,
  type Edge,
  type Node,
} from "@xyflow/react";
import type {
  ArchitectureEdge,
  ArchitectureNode,
  SimulationResultResponse,
} from "../types";

type CustomArchitectureGraphProps = {
  nodes: ArchitectureNode[];
  edges: ArchitectureEdge[];
  simulation: SimulationResultResponse | null;
};

function getNodeImpactClass(
  label: string,
  simulation: SimulationResultResponse | null
): string {
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

function isEdgeImpacted(
  sourceLabel: string,
  targetLabel: string,
  simulation: SimulationResultResponse | null
): boolean {
  if (!simulation) {
    return false;
  }

  return simulation.impactPaths.some((path: string[]) =>
    path.some((node: string, index: number) => {
      const nextNode = path[index + 1];
      return node === sourceLabel && nextNode === targetLabel;
    })
  );
}

function getNodePosition(
  node: ArchitectureNode,
  index: number
): { x: number; y: number } {
  const typeColumnMap: Record<string, number> = {
    database: 0,
    external: 0,
    queue: 0,
    service: 1,
    worker: 1,
    frontend: 2,
  };

  const column = typeColumnMap[node.type] ?? 1;

  return {
    x: column * 320,
    y: index * 95 + 80,
  };
}

export default function CustomArchitectureGraph({
  nodes,
  edges,
  simulation,
}: CustomArchitectureGraphProps) {
  const labelToId = useMemo(() => {
    return nodes.reduce<Record<string, string>>((accumulator, node) => {
      accumulator[node.label] = node.id;
      return accumulator;
    }, {});
  }, [nodes]);

  const flowNodes: Node[] = useMemo(() => {
    return nodes.map((node, index) => ({
      id: node.id,
      position: getNodePosition(node, index),
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
  }, [nodes, simulation]);

  const flowEdges: Edge[] = useMemo(() => {
    return edges
      .filter((edge) => labelToId[edge.sourceNode] && labelToId[edge.targetNode])
      .map((edge) => {
        const impacted = isEdgeImpacted(
          edge.sourceNode,
          edge.targetNode,
          simulation
        );

        return {
          id: edge.id,
          source: labelToId[edge.sourceNode],
          target: labelToId[edge.targetNode],
          label: edge.relationship,
          className: impacted ? "edge-impacted" : "edge-neutral",
          markerEnd: {
            type: MarkerType.ArrowClosed,
          },
        };
      });
  }, [edges, labelToId, simulation]);

  return (
    <div className="custom-graph-card">
      <div className="custom-graph-header">
        <div>
          <p className="custom-builder__eyebrow">Live Graph</p>
          <h3>Custom Architecture Map</h3>
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

      <div className="custom-flow-wrapper">
        {nodes.length === 0 ? (
          <div className="custom-graph-empty">
            Add nodes and dependencies to render your custom graph.
          </div>
        ) : (
          <ReactFlow nodes={flowNodes} edges={flowEdges} fitView>
            <Background />
            <Controls showInteractive={false} />
          </ReactFlow>
        )}
      </div>
    </div>
  );
}