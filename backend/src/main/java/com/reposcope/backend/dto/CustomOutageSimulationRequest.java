package com.reposcope.backend.dto;

import java.util.List;

public class CustomOutageSimulationRequest {
    private String failedNode;
    private List<CustomNodeRequest> nodes;
    private List<CustomEdgeRequest> edges;

    public String getFailedNode() {
        return failedNode;
    }

    public void setFailedNode(String failedNode) {
        this.failedNode = failedNode;
    }

    public List<CustomNodeRequest> getNodes() {
        return nodes;
    }

    public void setNodes(List<CustomNodeRequest> nodes) {
        this.nodes = nodes;
    }

    public List<CustomEdgeRequest> getEdges() {
        return edges;
    }

    public void setEdges(List<CustomEdgeRequest> edges) {
        this.edges = edges;
    }

    public static class CustomNodeRequest {
        private String id;
        private String label;
        private String type;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class CustomEdgeRequest {
        private String id;
        private String sourceNode;
        private String targetNode;
        private String relationship;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSourceNode() {
            return sourceNode;
        }

        public void setSourceNode(String sourceNode) {
            this.sourceNode = sourceNode;
        }

        public String getTargetNode() {
            return targetNode;
        }

        public void setTargetNode(String targetNode) {
            this.targetNode = targetNode;
        }

        public String getRelationship() {
            return relationship;
        }

        public void setRelationship(String relationship) {
            this.relationship = relationship;
        }
    }
}