package com.reposcope.backend.dto;

import java.util.List;

public class CustomSchemaChangeSimulationRequest {
    private List<ArchitectureNodeDto> nodes;
    private List<ArchitectureEdgeDto> edges;
    private String changedNode;

    public CustomSchemaChangeSimulationRequest() {
    }

    public List<ArchitectureNodeDto> getNodes() {
        return nodes;
    }

    public void setNodes(List<ArchitectureNodeDto> nodes) {
        this.nodes = nodes;
    }

    public List<ArchitectureEdgeDto> getEdges() {
        return edges;
    }

    public void setEdges(List<ArchitectureEdgeDto> edges) {
        this.edges = edges;
    }

    public String getChangedNode() {
        return changedNode;
    }

    public void setChangedNode(String changedNode) {
        this.changedNode = changedNode;
    }

    public static class ArchitectureNodeDto {
        private String id;
        private String label;
        private String type;

        private boolean containsPii;
        private String dataSensitivity;
        private List<String> complianceTags;

        public ArchitectureNodeDto() {
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
            return dataSensitivity == null ? "none" : dataSensitivity;
        }

        public void setDataSensitivity(String dataSensitivity) {
            this.dataSensitivity = dataSensitivity == null ? "none" : dataSensitivity;
        }

        public List<String> getComplianceTags() {
            return complianceTags == null ? List.of() : complianceTags;
        }

        public void setComplianceTags(List<String> complianceTags) {
            this.complianceTags = complianceTags == null ? List.of() : complianceTags;
        }
    }

    public static class ArchitectureEdgeDto {
        private String id;
        private String sourceNode;
        private String targetNode;
        private String relationship;

        public ArchitectureEdgeDto() {
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