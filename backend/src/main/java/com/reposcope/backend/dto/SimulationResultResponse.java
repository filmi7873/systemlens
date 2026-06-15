package com.reposcope.backend.dto;

import java.util.List;

public class SimulationResultResponse {
    private String failedNode;
    private String severity;
    private List<String> directlyAffected;
    private List<String> indirectlyAffected;
    private List<String> unaffected;
    private List<List<String>> impactPaths;
    private String explanation;

    public SimulationResultResponse(
            String failedNode,
            String severity,
            List<String> directlyAffected,
            List<String> indirectlyAffected,
            List<String> unaffected,
            List<List<String>> impactPaths,
            String explanation
    ) {
        this.failedNode = failedNode;
        this.severity = severity;
        this.directlyAffected = directlyAffected;
        this.indirectlyAffected = indirectlyAffected;
        this.unaffected = unaffected;
        this.impactPaths = impactPaths;
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

    public List<List<String>> getImpactPaths() {
        return impactPaths;
    }

    public String getExplanation() {
        return explanation;
    }
}