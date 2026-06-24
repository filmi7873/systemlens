package com.reposcope.backend.dto;

import java.util.List;

public class ArchitectureGraphResponse {
    private List<ArchitectureNodeResponse> nodes;
    private List<ArchitectureEdgeResponse> edges;

    public ArchitectureGraphResponse() {
    }

    public ArchitectureGraphResponse(
            List<ArchitectureNodeResponse> nodes,
            List<ArchitectureEdgeResponse> edges
    ) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public List<ArchitectureNodeResponse> getNodes() {
        return nodes;
    }

    public void setNodes(List<ArchitectureNodeResponse> nodes) {
        this.nodes = nodes;
    }

    public List<ArchitectureEdgeResponse> getEdges() {
        return edges;
    }

    public void setEdges(List<ArchitectureEdgeResponse> edges) {
        this.edges = edges;
    }

    public static class ArchitectureNodeResponse {
        private String id;
        private String label;
        private String type;

        private boolean containsPii;
        private String dataSensitivity;
        private List<String> complianceTags;

        public ArchitectureNodeResponse() {
        }

        public ArchitectureNodeResponse(String id, String label, String type) {
            this.id = id;
            this.label = label;
            this.type = type;
            this.containsPii = false;
            this.dataSensitivity = "none";
            this.complianceTags = List.of();
        }

        public ArchitectureNodeResponse(
                String id,
                String label,
                String type,
                boolean containsPii,
                String dataSensitivity,
                List<String> complianceTags
        ) {
            this.id = id;
            this.label = label;
            this.type = type;
            this.containsPii = containsPii;
            this.dataSensitivity = dataSensitivity == null ? "none" : dataSensitivity;
            this.complianceTags = complianceTags == null ? List.of() : complianceTags;
        }

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

        public boolean isContainsPii() {
            return containsPii;
        }

        public void setContainsPii(boolean containsPii) {
            this.containsPii = containsPii;
        }

        public String getDataSensitivity() {
            return dataSensitivity;
        }

        public void setDataSensitivity(String dataSensitivity) {
            this.dataSensitivity = dataSensitivity == null ? "none" : dataSensitivity;
        }

        public List<String> getComplianceTags() {
            return complianceTags;
        }

        public void setComplianceTags(List<String> complianceTags) {
            this.complianceTags = complianceTags == null ? List.of() : complianceTags;
        }
    }

    public static class ArchitectureEdgeResponse {
        private String id;
        private String sourceNode;
        private String targetNode;
        private String relationship;

        public ArchitectureEdgeResponse() {
        }

        public ArchitectureEdgeResponse(
                String id,
                String sourceNode,
                String targetNode,
                String relationship
        ) {
            this.id = id;
            this.sourceNode = sourceNode;
            this.targetNode = targetNode;
            this.relationship = relationship;
        }

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