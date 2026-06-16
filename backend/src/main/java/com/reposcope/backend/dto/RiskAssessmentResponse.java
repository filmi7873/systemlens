package com.reposcope.backend.dto;

import java.util.List;

public class RiskAssessmentResponse {
    private int riskScore;
    private String riskLevel;
    private List<String> riskFactors;
    private List<String> recommendations;

    public RiskAssessmentResponse(
            int riskScore,
            String riskLevel,
            List<String> riskFactors,
            List<String> recommendations
    ) {
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.riskFactors = riskFactors;
        this.recommendations = recommendations;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public List<String> getRiskFactors() {
        return riskFactors;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }
}