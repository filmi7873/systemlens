package com.reposcope.backend.service;

import com.reposcope.backend.dto.SimulationResultResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SimulationService {

    public SimulationResultResponse runSamplePaymentOutage() {
        String failedNode = "Payment Provider";

        Map<String, List<String>> dependents = Map.of(
                "Payment Provider", List.of("Checkout Service"),
                "Checkout Service", List.of("Web App"),
                "Auth Service", List.of("Web App"),
                "Product Service", List.of("Web App"),
                "Inventory Service", List.of("Checkout Service", "Web App"),
                "Order Database", List.of("Checkout Service"),
                "Inventory Database", List.of("Inventory Service"),
                "Email Queue", List.of("Notification Worker"),
                "Notification Worker", List.of()
        );

        List<String> allNodes = List.of(
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

        List<String> directlyAffected = dependents.getOrDefault(failedNode, List.of());
        List<String> indirectlyAffected = findIndirectlyAffected(
                dependents,
                directlyAffected
        );

        List<String> affected = new ArrayList<>();
        affected.add(failedNode);
        affected.addAll(directlyAffected);
        affected.addAll(indirectlyAffected);

        List<String> unaffected = allNodes.stream()
                .filter(node -> !affected.contains(node))
                .toList();

        String severity = calculateSeverity(directlyAffected, indirectlyAffected);

        String explanation = "The Payment Provider is required by the Checkout Service. "
                + "When it fails, checkout is directly impacted. Because the Web App depends on "
                + "checkout for purchase completion, the user-facing application is indirectly impacted.";

        return new SimulationResultResponse(
                failedNode,
                severity,
                directlyAffected,
                indirectlyAffected,
                unaffected,
                explanation
        );
    }

    private List<String> findIndirectlyAffected(
            Map<String, List<String>> dependents,
            List<String> directlyAffected
    ) {
        List<String> indirectlyAffected = new ArrayList<>();

        for (String directNode : directlyAffected) {
            List<String> secondLevelDependents = dependents.getOrDefault(directNode, List.of());

            for (String node : secondLevelDependents) {
                if (!directlyAffected.contains(node) && !indirectlyAffected.contains(node)) {
                    indirectlyAffected.add(node);
                }
            }
        }

        return indirectlyAffected;
    }

    private String calculateSeverity(
            List<String> directlyAffected,
            List<String> indirectlyAffected
    ) {
        int impactCount = directlyAffected.size() + indirectlyAffected.size();

        if (impactCount >= 3) {
            return "high";
        }

        if (impactCount == 2) {
            return "medium";
        }

        return "low";
    }
}