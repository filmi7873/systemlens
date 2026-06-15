package com.reposcope.backend.service;

import com.reposcope.backend.dto.SimulationResultResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SimulationService {

    private final Map<String, List<String>> dependents = Map.of(
            "Payment Provider", List.of("Checkout Service"),
            "Checkout Service", List.of("Web App"),
            "Auth Service", List.of("Web App"),
            "Product Service", List.of("Web App"),
            "Inventory Service", List.of("Checkout Service", "Web App"),
            "Order Database", List.of("Checkout Service"),
            "Inventory Database", List.of("Inventory Service"),
            "Email Queue", List.of("Notification Worker"),
            "Notification Worker", List.of(),
            "Cart Service", List.of("Checkout Service")
    );

    private final List<String> allNodes = List.of(
            "Web App",
            "Auth Service",
            "Product Service",
            "Inventory Service",
            "Cart Service",
            "Checkout Service",
            "Payment Provider",
            "Order Database",
            "Inventory Database",
            "Email Queue",
            "Notification Worker"
    );

    public SimulationResultResponse runSamplePaymentOutage() {
        return runOutageSimulation("Payment Provider");
    }

    public SimulationResultResponse runOutageSimulation(String failedNode) {
        if (failedNode == null || failedNode.isBlank()) {
            throw new IllegalArgumentException("Failed node is required.");
        }

        if (!allNodes.contains(failedNode)) {
            throw new IllegalArgumentException("Unknown node: " + failedNode);
        }

        List<String> directlyAffected = dependents.getOrDefault(failedNode, List.of());

        Set<String> indirectlyAffectedSet = new LinkedHashSet<>();

        for (String directNode : directlyAffected) {
            collectDownstreamImpact(directNode, indirectlyAffectedSet);
        }

        indirectlyAffectedSet.removeAll(directlyAffected);
        indirectlyAffectedSet.remove(failedNode);

        List<String> indirectlyAffected = new ArrayList<>(indirectlyAffectedSet);

        List<String> affected = new ArrayList<>();
        affected.add(failedNode);
        affected.addAll(directlyAffected);
        affected.addAll(indirectlyAffected);

        List<String> unaffected = allNodes.stream()
                .filter(node -> !affected.contains(node))
                .toList();

        String severity = calculateSeverity(directlyAffected, indirectlyAffected);

        String explanation = buildExplanation(
                failedNode,
                directlyAffected,
                indirectlyAffected,
                severity
        );

        return new SimulationResultResponse(
                failedNode,
                severity,
                directlyAffected,
                indirectlyAffected,
                unaffected,
                explanation
        );
    }

    public List<String> getSampleNodes() {
        return allNodes;
    }

    private void collectDownstreamImpact(String node, Set<String> impactedNodes) {
        List<String> nextDependents = dependents.getOrDefault(node, List.of());

        for (String dependent : nextDependents) {
            if (impactedNodes.add(dependent)) {
                collectDownstreamImpact(dependent, impactedNodes);
            }
        }
    }

    private String calculateSeverity(
            List<String> directlyAffected,
            List<String> indirectlyAffected
    ) {
        int impactCount = directlyAffected.size() + indirectlyAffected.size();

        if (impactCount >= 4) {
            return "high";
        }

        if (impactCount >= 2) {
            return "medium";
        }

        return "low";
    }

    private String buildExplanation(
            String failedNode,
            List<String> directlyAffected,
            List<String> indirectlyAffected,
            String severity
    ) {
        if (directlyAffected.isEmpty()) {
            return failedNode + " failed, but no downstream systems depend on it in this sample architecture. Severity is " + severity + ".";
        }

        String directText = String.join(", ", directlyAffected);

        if (indirectlyAffected.isEmpty()) {
            return failedNode + " failed. The directly affected system is: " + directText + ". Severity is " + severity + ".";
        }

        String indirectText = String.join(", ", indirectlyAffected);

        return failedNode + " failed. Directly affected systems: " + directText
                + ". Indirectly affected systems: " + indirectText
                + ". Severity is " + severity + " because impact spreads through the dependency graph.";
    }
}