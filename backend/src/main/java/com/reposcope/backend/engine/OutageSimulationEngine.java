package com.reposcope.backend.engine;

import com.reposcope.backend.dto.SimulationResultResponse;
import com.reposcope.backend.model.ArchitectureGraph;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class OutageSimulationEngine {

    public SimulationResultResponse simulate(ArchitectureGraph graph, String failedNode) {
        validateInput(graph, failedNode);

        Map<String, List<String>> dependents = graph.buildDependentsMap();

        List<String> directlyAffected = dependents.getOrDefault(failedNode, List.of());

        Set<String> indirectlyAffectedSet = new LinkedHashSet<>();

        for (String directNode : directlyAffected) {
            collectDownstreamImpact(directNode, dependents, indirectlyAffectedSet);
        }

        indirectlyAffectedSet.removeAll(directlyAffected);
        indirectlyAffectedSet.remove(failedNode);

        List<String> indirectlyAffected = new ArrayList<>(indirectlyAffectedSet);

        List<String> affected = new ArrayList<>();
        affected.add(failedNode);
        affected.addAll(directlyAffected);
        affected.addAll(indirectlyAffected);

        List<String> unaffected = graph.getNodeLabels().stream()
                .filter(node -> !affected.contains(node))
                .toList();

        List<List<String>> impactPaths = buildImpactPaths(failedNode, dependents);

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
                impactPaths,
                explanation
        );
    }

    private void validateInput(ArchitectureGraph graph, String failedNode) {
        if (failedNode == null || failedNode.isBlank()) {
            throw new IllegalArgumentException("Failed node is required.");
        }

        if (!graph.containsNode(failedNode)) {
            throw new IllegalArgumentException("Unknown node: " + failedNode);
        }
    }

    private void collectDownstreamImpact(
            String node,
            Map<String, List<String>> dependents,
            Set<String> impactedNodes
    ) {
        List<String> nextDependents = dependents.getOrDefault(node, List.of());

        for (String dependent : nextDependents) {
            if (impactedNodes.add(dependent)) {
                collectDownstreamImpact(dependent, dependents, impactedNodes);
            }
        }
    }

   private List<List<String>> buildImpactPaths(
        String failedNode,
        Map<String, List<String>> dependents
) {
    List<List<String>> paths = new ArrayList<>();
    List<String> currentPath = new ArrayList<>();
    currentPath.add(failedNode);

    Set<String> visited = new LinkedHashSet<>();
    visited.add(failedNode);

    collectImpactPaths(failedNode, dependents, currentPath, paths, visited);

    return paths;
}

private void collectImpactPaths(
        String currentNode,
        Map<String, List<String>> dependents,
        List<String> currentPath,
        List<List<String>> paths,
        Set<String> visited
) {
    List<String> nextDependents = dependents.getOrDefault(currentNode, List.of());

    for (String dependent : nextDependents) {
        if (visited.contains(dependent)) {
            continue;
        }

        List<String> newPath = new ArrayList<>(currentPath);
        newPath.add(dependent);
        paths.add(newPath);

        Set<String> newVisited = new LinkedHashSet<>(visited);
        newVisited.add(dependent);

        collectImpactPaths(dependent, dependents, newPath, paths, newVisited);
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
            return failedNode + " failed, but no downstream systems depend on it in this architecture. Severity is " + severity + ".";
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