package com.reposcope.backend.dto;

import java.util.List;

public class SimulationResultResponse {
    private String failedNode;
    private String severity;
    private List<String> directlyAffected;
    private List<String> indirectlyAffected;
    private List<String> unaffected;
    private String explanation;

    public SimulationResultResponse(
            String failedNode,
            String severity,
            List<String> directlyAffected,
            List<String> indirectlyAffected,
            List<String> unaffected,
            String explanation
    ) {
        this.failedNode = failedNode;
        this.severity = severity;
        this.directlyAffected = directlyAffected;
        this.indirectlyAffected = indirectlyAffected;
        this.unaffected = unaffected;
        this.explanation = explanation;
    }

    public String getFailedNode() {
        return failedNode;
    }

    public String getSeverity() {
        return severity;
    }

    public List<String> getDirectlyAffected() {
        return directlyAffected;
    }

    public List<String> getIndirectlyAffected() {
        return indirectlyAffected;
    }

    public List<String> getUnaffected() {
        return unaffected;
    }

    public String getExplanation() {
        return explanation;
    }
}