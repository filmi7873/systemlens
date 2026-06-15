package com.reposcope.backend.service;

import com.reposcope.backend.dto.SimulationResultResponse;
import org.springframework.stereotype.Service;
import com.reposcope.backend.dto.ArchitectureGraphResponse;

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
    
    public ArchitectureGraphResponse getSampleArchitectureGraph() {
    List<ArchitectureGraphResponse.ArchitectureNode> nodes = List.of(
            new ArchitectureGraphResponse.ArchitectureNode("web-app", "Web App", "frontend"),
            new ArchitectureGraphResponse.ArchitectureNode("auth-service", "Auth Service", "service"),
            new ArchitectureGraphResponse.ArchitectureNode("product-service", "Product Service", "service"),
            new ArchitectureGraphResponse.ArchitectureNode("inventory-service", "Inventory Service", "service"),
            new ArchitectureGraphResponse.ArchitectureNode("cart-service", "Cart Service", "service"),
            new ArchitectureGraphResponse.ArchitectureNode("checkout-service", "Checkout Service", "service"),
            new ArchitectureGraphResponse.ArchitectureNode("payment-provider", "Payment Provider", "external"),
            new ArchitectureGraphResponse.ArchitectureNode("order-database", "Order Database", "database"),
            new ArchitectureGraphResponse.ArchitectureNode("inventory-database", "Inventory Database", "database"),
            new ArchitectureGraphResponse.ArchitectureNode("email-queue", "Email Queue", "queue"),
            new ArchitectureGraphResponse.ArchitectureNode("notification-worker", "Notification Worker", "worker")
    );

    List<ArchitectureGraphResponse.ArchitectureEdge> edges = List.of(
            new ArchitectureGraphResponse.ArchitectureEdge("edge-auth-web", "auth-service", "web-app", "supports"),
            new ArchitectureGraphResponse.ArchitectureEdge("edge-product-web", "product-service", "web-app", "supports"),
            new ArchitectureGraphResponse.ArchitectureEdge("edge-inventory-web", "inventory-service", "web-app", "supports"),
            new ArchitectureGraphResponse.ArchitectureEdge("edge-cart-checkout", "cart-service", "checkout-service", "supports"),
            new ArchitectureGraphResponse.ArchitectureEdge("edge-inventory-checkout", "inventory-service", "checkout-service", "supports"),
            new ArchitectureGraphResponse.ArchitectureEdge("edge-checkout-web", "checkout-service", "web-app", "supports"),
            new ArchitectureGraphResponse.ArchitectureEdge("edge-payment-checkout", "payment-provider", "checkout-service", "supports"),
            new ArchitectureGraphResponse.ArchitectureEdge("edge-order-checkout", "order-database", "checkout-service", "supports"),
            new ArchitectureGraphResponse.ArchitectureEdge("edge-inventory-db-service", "inventory-database", "inventory-service", "supports"),
            new ArchitectureGraphResponse.ArchitectureEdge("edge-email-worker", "email-queue", "notification-worker", "supports")
    );

    return new ArchitectureGraphResponse(nodes, edges);
}

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