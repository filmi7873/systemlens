package com.reposcope.backend.dto;

import java.util.List;

public class ArchitectureGraphResponse {
    private List<ArchitectureNode> nodes;
    private List<ArchitectureEdge> edges;

    public ArchitectureGraphResponse(List<ArchitectureNode> nodes, List<ArchitectureEdge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<ArchitectureNode> getNodes() {
        return nodes;
    }

    public List<ArchitectureEdge> getEdges() {
        return edges;
    }

    public static class ArchitectureNode {
        private String id;
        private String label;
        private String type;

        public ArchitectureNode(String id, String label, String type) {
            this.id = id;
            this.label = label;
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        public String getType() {
            return type;
        }
    }

    public static class ArchitectureEdge {
        private String id;
        private String source;
        private String target;
        private String relationship;

        public ArchitectureEdge(String id, String source, String target, String relationship) {
            this.id = id;
            this.source = source;
            this.target = target;
            this.relationship = relationship;
        }

        public String getId() {
            return id;
        }

        public String getSource() {
            return source;
        }

        public String getTarget() {
            return target;
        }

        public String getRelationship() {
            return relationship;
        }
    }
}