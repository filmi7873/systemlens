package com.reposcope.backend.dto;

public class OutageSimulationRequest {
    private String failedNode;

    public String getFailedNode() {
        return failedNode;
    }

    public void setFailedNode(String failedNode) {
        this.failedNode = failedNode;
    }
}