package com.reposcope.backend.model;

public class SystemEdge {
    private final String id;
    private final String sourceNode;
    private final String targetNode;
    private final String relationship;

    public SystemEdge(String id, String sourceNode, String targetNode, String relationship) {
        this.id = id;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.relationship = relationship;
    }

    public String getId() {
        return id;
    }

    public String getSourceNode() {
        return sourceNode;
    }

    public String getTargetNode() {
        return targetNode;
    }

    public String getRelationship() {
        return relationship;
    }
}