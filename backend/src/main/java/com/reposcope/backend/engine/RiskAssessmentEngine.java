package com.reposcope.backend.engine;

import com.reposcope.backend.dto.RiskAssessmentResponse;
import com.reposcope.backend.dto.SimulationResultResponse;
import com.reposcope.backend.model.ArchitectureGraph;
import com.reposcope.backend.model.SystemNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class RiskAssessmentEngine {

    public RiskAssessmentResponse assess(
            ArchitectureGraph graph,
            SimulationResultResponse simulation,
            String simulationType
    ) {
        List<String> riskFactors = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();

        int riskScore = 0;

        String sourceNode = simulation.getFailedNode();
        String sourceType = getNodeType(graph, sourceNode);

        int impactedCount = simulation.getDirectlyAffected().size()
                + simulation.getIndirectlyAffected().size();

        int longestPathLength = simulation.getImpactPaths()
                .stream()
                .map(List::size)
                .max(Comparator.naturalOrder())
                .orElse(1);

        if (sourceType.equalsIgnoreCase("database")) {
            riskScore += 25;
            riskFactors.add("The change originates from a database, which can affect downstream services that depend on its schema.");
            recommendations.add("Prepare or verify a rollback plan before applying database changes.");
        }

        if (sourceType.equalsIgnoreCase("external")) {
            riskScore += 20;
            riskFactors.add("The impact starts from an external provider, which may be harder to control or rollback.");
            recommendations.add("Verify provider fallback behavior and error handling.");
        }

        if (impactedCount >= 3) {
            riskScore += 25;
            riskFactors.add("The simulation affects " + impactedCount + " downstream systems, increasing blast radius.");
            recommendations.add("Coordinate validation across all affected services before deployment.");
        } else if (impactedCount >= 1) {
            riskScore += 12;
            riskFactors.add("The simulation affects " + impactedCount + " downstream system(s).");
        }

        if (longestPathLength >= 4) {
            riskScore += 20;
            riskFactors.add("The longest impact path spans " + longestPathLength + " nodes, meaning impact propagates across multiple architectural layers.");
            recommendations.add("Validate the full propagation path, not just the directly affected service.");
        } else if (longestPathLength >= 3) {
            riskScore += 10;
            riskFactors.add("Impact propagates beyond the first downstream dependency.");
        }

        if (touchesNodeType(graph, simulation, "frontend")) {
            riskScore += 15;
            riskFactors.add("The impact reaches a frontend or customer-facing surface.");
            recommendations.add("Run a customer-facing smoke test after backend validation.");
        }

        if (touchesImportantBusinessFlow(simulation)) {
            riskScore += 20;
            riskFactors.add("The impact touches a checkout, payment, order, or cart-related path.");
            recommendations.add("Smoke test the checkout/payment flow before release.");
        }

        if (simulationType.equalsIgnoreCase("schema-change")) {
            riskScore += 10;
            riskFactors.add("Schema or contract changes may require consumer validation even if services remain online.");
            recommendations.add("Run integration tests for direct consumers of the changed contract.");
        }

        if (riskFactors.isEmpty()) {
            riskFactors.add("No major downstream risk factors were detected in the current architecture graph.");
            recommendations.add("Run standard regression checks for the changed component.");
        }

        int cappedScore = Math.min(riskScore, 100);
        String riskLevel = calculateRiskLevel(cappedScore);

        return new RiskAssessmentResponse(
                cappedScore,
                riskLevel,
                riskFactors,
                recommendations
        );
    }

    private String calculateRiskLevel(int riskScore) {
        if (riskScore >= 70) {
            return "high";
        }

        if (riskScore >= 35) {
            return "medium";
        }

        return "low";
    }

    private boolean touchesNodeType(
            ArchitectureGraph graph,
            SimulationResultResponse simulation,
            String nodeType
    ) {
        List<String> affectedNodes = new ArrayList<>();
        affectedNodes.add(simulation.getFailedNode());
        affectedNodes.addAll(simulation.getDirectlyAffected());
        affectedNodes.addAll(simulation.getIndirectlyAffected());

        return graph.getNodes()
                .stream()
                .filter(node -> affectedNodes.contains(node.getLabel()))
                .anyMatch(node -> node.getType().equalsIgnoreCase(nodeType));
    }

    private boolean touchesImportantBusinessFlow(SimulationResultResponse simulation) {
        List<String> affectedNodes = new ArrayList<>();
        affectedNodes.add(simulation.getFailedNode());
        affectedNodes.addAll(simulation.getDirectlyAffected());
        affectedNodes.addAll(simulation.getIndirectlyAffected());

        return affectedNodes.stream()
                .map(String::toLowerCase)
                .anyMatch(node ->
                        node.contains("checkout")
                                || node.contains("payment")
                                || node.contains("order")
                                || node.contains("cart")
                );
    }

    private String getNodeType(ArchitectureGraph graph, String nodeLabel) {
        return graph.getNodes()
                .stream()
                .filter(node -> node.getLabel().equals(nodeLabel))
                .map(SystemNode::getType)
                .findFirst()
                .orElse("unknown");
    }
}