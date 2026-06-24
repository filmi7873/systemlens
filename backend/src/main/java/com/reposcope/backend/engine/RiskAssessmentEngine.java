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

        boolean isSchemaChange = simulationType.equalsIgnoreCase("schema-change");

        if (sourceType.equalsIgnoreCase("database")) {
            riskScore += 20;

            if (isSchemaChange) {
                riskFactors.add("The change originates from a database, which can affect downstream services that depend on its schema.");
                recommendations.add("Prepare or verify a rollback plan before applying database changes.");
            } else {
                riskFactors.add("The outage originates from a database, which can affect downstream services that depend on its availability.");
                recommendations.add("Verify database recovery steps and confirm downstream services reconnect cleanly.");
            }
        }

        if (sourceType.equalsIgnoreCase("external")) {
            riskScore += 15;

            if (isSchemaChange) {
                riskFactors.add("The change starts from an external provider, which may be harder to control or rollback.");
                recommendations.add("Verify provider contract changes, fallback behavior, and error handling.");
            } else {
                riskFactors.add("The outage starts from an external provider, which may be harder to control or recover quickly.");
                recommendations.add("Verify provider fallback behavior, timeout handling, and customer-facing error states.");
            }
        }

        if (impactedCount >= 3) {
            riskScore += 20;
            riskFactors.add("The simulation affects " + impactedCount + " downstream systems, increasing blast radius.");
            recommendations.add("Coordinate validation across all affected services before deployment.");
        } else if (impactedCount >= 1) {
            riskScore += 10;
            riskFactors.add("The simulation affects " + impactedCount + " downstream system(s).");
        }

        if (longestPathLength >= 4) {
            riskScore += 15;
            riskFactors.add("The longest impact path spans " + longestPathLength + " nodes, meaning impact propagates across multiple architectural layers.");
            recommendations.add("Validate the full propagation path, not just the directly affected service.");
        } else if (longestPathLength >= 3) {
            riskScore += 8;
            riskFactors.add("Impact propagates beyond the first downstream dependency.");
            recommendations.add("Validate indirect consumers, not just the first affected service.");
        }

        if (touchesNodeType(graph, simulation, "frontend")) {
            riskScore += 12;
            riskFactors.add("The impact reaches a frontend or customer-facing surface.");
            recommendations.add("Run a customer-facing smoke test after backend validation.");
        }

        if (touchesImportantBusinessFlow(simulation)) {
            riskScore += 15;
            riskFactors.add("The impact touches a checkout, payment, order, or cart-related path.");
            recommendations.add("Smoke test the checkout/payment flow before release.");
        }

        if (isSchemaChange) {
            riskScore += 8;
            riskFactors.add("Schema or contract changes may require consumer validation even if services remain online.");
            recommendations.add("Run integration tests for direct consumers of the changed contract.");
        }

        List<SystemNode> affectedSystemNodes = getAffectedSystemNodes(graph, simulation);

        riskScore += calculateComplianceRiskScore(affectedSystemNodes);
        addComplianceRiskFactors(riskFactors, affectedSystemNodes);
        addComplianceRecommendations(recommendations, affectedSystemNodes);

        if (riskFactors.isEmpty()) {
            riskFactors.add("No major downstream risk factors were detected in the current architecture graph.");
            recommendations.add("Run standard regression checks for the source component.");
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
        List<String> affectedNodes = getAffectedNodeLabels(simulation);

        return graph.getNodes()
                .stream()
                .filter(node -> affectedNodes.contains(node.getLabel()))
                .anyMatch(node -> node.getType().equalsIgnoreCase(nodeType));
    }

    private boolean touchesImportantBusinessFlow(SimulationResultResponse simulation) {
        List<String> affectedNodes = getAffectedNodeLabels(simulation);

        return affectedNodes.stream()
                .map(String::toLowerCase)
                .anyMatch(node ->
                        node.contains("checkout")
                                || node.contains("payment")
                                || node.contains("order")
                                || node.contains("cart")
                );
    }

    private int calculateComplianceRiskScore(List<SystemNode> affectedSystemNodes) {
        int score = 0;

        for (SystemNode node : affectedSystemNodes) {
            if (node.isContainsPii()) {
                score += 15;
            }

            String sensitivity = node.getDataSensitivity();

            if (sensitivity.equalsIgnoreCase("internal")) {
                score += 5;
            } else if (sensitivity.equalsIgnoreCase("confidential")) {
                score += 10;
            } else if (sensitivity.equalsIgnoreCase("restricted")) {
                score += 20;
            }

            if (hasComplianceTag(node, "PII")) {
                score += 10;
            }

            if (hasComplianceTag(node, "PCI")) {
                score += 15;
            }

            if (hasComplianceTag(node, "HIPAA")) {
                score += 15;
            }

            if (hasComplianceTag(node, "SOC2")) {
                score += 10;
            }

            if (hasComplianceTag(node, "GDPR")) {
                score += 10;
            }
        }

        return score;
    }

    private void addComplianceRiskFactors(
            List<String> riskFactors,
            List<SystemNode> affectedSystemNodes
    ) {
        boolean affectsPii = affectedSystemNodes.stream()
                .anyMatch(SystemNode::isContainsPii);

        boolean affectsRestrictedData = affectedSystemNodes.stream()
                .anyMatch(node -> node.getDataSensitivity().equalsIgnoreCase("restricted"));

        boolean affectsConfidentialData = affectedSystemNodes.stream()
                .anyMatch(node -> node.getDataSensitivity().equalsIgnoreCase("confidential"));

        boolean affectsComplianceScopedSystem = affectedSystemNodes.stream()
                .anyMatch(node -> !node.getComplianceTags().isEmpty());

        boolean affectsPaymentCompliance = affectedSystemNodes.stream()
                .anyMatch(node -> hasComplianceTag(node, "PCI"));

        boolean affectsHealthCompliance = affectedSystemNodes.stream()
                .anyMatch(node -> hasComplianceTag(node, "HIPAA"));

        if (affectsPii) {
            riskFactors.add("The simulation affects systems that handle personally identifiable information.");
        }

        if (affectsRestrictedData) {
            riskFactors.add("The simulation affects systems marked as restricted data environments.");
        } else if (affectsConfidentialData) {
            riskFactors.add("The simulation affects systems marked as confidential data environments.");
        }

        if (affectsComplianceScopedSystem) {
            riskFactors.add("The simulation affects systems with compliance-related tags.");
        }

        if (affectsPaymentCompliance) {
            riskFactors.add("The affected path includes PCI-scoped payment or cardholder-data systems.");
        }

        if (affectsHealthCompliance) {
            riskFactors.add("The affected path includes HIPAA-scoped health-data systems.");
        }
    }

    private void addComplianceRecommendations(
            List<String> recommendations,
            List<SystemNode> affectedSystemNodes
    ) {
        boolean affectsPii = affectedSystemNodes.stream()
                .anyMatch(SystemNode::isContainsPii);

        boolean affectsRestrictedData = affectedSystemNodes.stream()
                .anyMatch(node -> node.getDataSensitivity().equalsIgnoreCase("restricted"));

        boolean affectsComplianceScopedSystem = affectedSystemNodes.stream()
                .anyMatch(node -> !node.getComplianceTags().isEmpty());

        boolean affectsPaymentCompliance = affectedSystemNodes.stream()
                .anyMatch(node -> hasComplianceTag(node, "PCI"));

        boolean affectsHealthCompliance = affectedSystemNodes.stream()
                .anyMatch(node -> hasComplianceTag(node, "HIPAA"));

        if (affectsPii) {
            recommendations.add("Verify that affected PII-handling systems preserve access controls, audit logs, and data protection requirements.");
        }

        if (affectsRestrictedData) {
            recommendations.add("Review recovery and validation steps for restricted data systems before restoring full traffic.");
        }

        if (affectsComplianceScopedSystem) {
            recommendations.add("Confirm compliance-sensitive workflows are included in post-change validation and incident review.");
        }

        if (affectsPaymentCompliance) {
            recommendations.add("Validate payment-related controls, logging, and error handling before releasing or recovering PCI-scoped systems.");
        }

        if (affectsHealthCompliance) {
            recommendations.add("Review health-data handling, access controls, and audit requirements before restoring HIPAA-scoped workflows.");
        }
    }

    private List<SystemNode> getAffectedSystemNodes(
            ArchitectureGraph graph,
            SimulationResultResponse simulation
    ) {
        List<String> affectedNodeLabels = getAffectedNodeLabels(simulation);

        return graph.getNodes()
                .stream()
                .filter(node -> affectedNodeLabels.contains(node.getLabel()))
                .toList();
    }

    private List<String> getAffectedNodeLabels(SimulationResultResponse simulation) {
        List<String> affectedNodes = new ArrayList<>();

        affectedNodes.add(simulation.getFailedNode());
        affectedNodes.addAll(simulation.getDirectlyAffected());
        affectedNodes.addAll(simulation.getIndirectlyAffected());

        return affectedNodes;
    }

    private boolean hasComplianceTag(SystemNode node, String tag) {
        return node.getComplianceTags()
                .stream()
                .anyMatch(existingTag -> existingTag.equalsIgnoreCase(tag));
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