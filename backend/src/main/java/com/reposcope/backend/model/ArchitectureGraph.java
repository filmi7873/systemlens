package com.reposcope.backend.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ArchitectureGraph {
    private final List<SystemNode> nodes;
    private final List<SystemEdge> edges;

    public ArchitectureGraph(List<SystemNode> nodes, List<SystemEdge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<SystemNode> getNodes() {
        return nodes;
    }

    public List<SystemEdge> getEdges() {
        return edges;
    }

    public boolean containsNode(String nodeLabel) {
        return nodes.stream()
                .anyMatch(node -> node.getLabel().equals(nodeLabel));
    }

    public List<String> getNodeLabels() {
        return nodes.stream()
                .map(SystemNode::getLabel)
                .toList();
    }

    public Map<String, List<String>> buildDependentsMap() {
        return edges.stream()
                .collect(Collectors.groupingBy(
                        SystemEdge::getSourceNode,
                        Collectors.mapping(SystemEdge::getTargetNode, Collectors.toList())
                ));
    }
}