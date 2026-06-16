package com.reposcope.backend.dto;

public class SchemaChangeSimulationRequest {
    private String changedNode;

    public String getChangedNode() {
        return changedNode;
    }

    public void setChangedNode(String changedNode) {
        this.changedNode = changedNode;
    }
}