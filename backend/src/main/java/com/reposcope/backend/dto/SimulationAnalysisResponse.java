package com.reposcope.backend.dto;

public class SimulationAnalysisResponse {
    private SimulationResultResponse simulation;
    private RiskAssessmentResponse riskAssessment;

    public SimulationAnalysisResponse(
            SimulationResultResponse simulation,
            RiskAssessmentResponse riskAssessment
    ) {
        this.simulation = simulation;
        this.riskAssessment = riskAssessment;
    }

    public SimulationResultResponse getSimulation() {
        return simulation;
    }

    public RiskAssessmentResponse getRiskAssessment() {
        return riskAssessment;
    }
}