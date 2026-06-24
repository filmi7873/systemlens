package com.reposcope.backend.engine;

import com.reposcope.backend.dto.SimulationResultResponse;
import com.reposcope.backend.model.ArchitectureGraph;
import com.reposcope.backend.model.SystemNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class AuthFailureSimulationEngine {

    public SimulationResultResponse simulate(ArchitectureGraph graph, String authNode) {
        validateInput(graph, authNode);

        Map<String, List<String>> dependents = graph.buildDependentsMap();

        List<String> directlyAffected = dependents.getOrDefault(authNode, List.of());

        Set<String> indirectlyAffectedSet = new LinkedHashSet<>();

        for (String directNode : directlyAffected) {
            collectDownstreamImpact(directNode, dependents, indirectlyAffectedSet);
        }

        indirectlyAffectedSet.removeAll(directlyAffected);
        indirectlyAffectedSet.remove(authNode);

        List<String> indirectlyAffected = new ArrayList<>(indirectlyAffectedSet);

        List<String> affected = new ArrayList<>();
        affected.add(authNode);
        affected.addAll(directlyAffected);
        affected.addAll(indirectlyAffected);

        List<String> unaffected = graph.getNodeLabels()
                .stream()
                .filter(node -> !affected.contains(node))
                .toList();

        List<List<String>> impactPaths = buildImpactPaths(authNode, dependents);

        String severity = calculateSeverity(
                graph,
                authNode,
                directlyAffected,
                indirectlyAffected
        );

        String explanation = buildExplanation(
                graph,
                authNode,
                directlyAffected,
                indirectlyAffected,
                severity
        );

        return new SimulationResultResponse(
                authNode,
                severity,
                directlyAffected,
                indirectlyAffected,
                unaffected,
                impactPaths,
                explanation
        );
    }

    private void validateInput(ArchitectureGraph graph, String authNode) {
        if (authNode == null || authNode.isBlank()) {
            throw new IllegalArgumentException("Auth node is required.");
        }

        if (!graph.containsNode(authNode)) {
            throw new IllegalArgumentException("Unknown node: " + authNode);
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
            String authNode,
            Map<String, List<String>> dependents
    ) {
        List<List<String>> paths = new ArrayList<>();
        List<String> currentPath = new ArrayList<>();
        currentPath.add(authNode);

        Set<String> visited = new LinkedHashSet<>();
        visited.add(authNode);

        collectImpactPaths(authNode, dependents, currentPath, paths, visited);

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
            ArchitectureGraph graph,
            String authNode,
            List<String> directlyAffected,
            List<String> indirectlyAffected
    ) {
        int impactCount = directlyAffected.size() + indirectlyAffected.size();

        boolean reachesFrontend = touchesNodeType(
                graph,
                directlyAffected,
                indirectlyAffected,
                "frontend"
        );

        boolean sourceLooksAuthCritical = looksAuthCritical(authNode);

        if ((sourceLooksAuthCritical && impactCount >= 2) || reachesFrontend) {
            return "high";
        }

        if (impactCount >= 2) {
            return "medium";
        }

        if (impactCount >= 1) {
            return "medium";
        }

        return "low";
    }

    private boolean touchesNodeType(
            ArchitectureGraph graph,
            List<String> directlyAffected,
            List<String> indirectlyAffected,
            String nodeType
    ) {
        List<String> affectedNodes = new ArrayList<>();
        affectedNodes.addAll(directlyAffected);
        affectedNodes.addAll(indirectlyAffected);

        return graph.getNodes()
                .stream()
                .filter(node -> affectedNodes.contains(node.getLabel()))
                .anyMatch(node -> node.getType().equalsIgnoreCase(nodeType));
    }

    private boolean looksAuthCritical(String nodeLabel) {
        String normalized = nodeLabel.toLowerCase();

        return normalized.contains("auth")
                || normalized.contains("identity")
                || normalized.contains("login")
                || normalized.contains("session")
                || normalized.contains("permission")
                || normalized.contains("sso")
                || normalized.contains("token");
    }

    private String buildExplanation(
            ArchitectureGraph graph,
            String authNode,
            List<String> directlyAffected,
            List<String> indirectlyAffected,
            String severity
    ) {
        String nodeType = getNodeType(graph, authNode);

        if (directlyAffected.isEmpty()) {
            return authNode + " failed as an authentication or authorization dependency, but no downstream systems depend on it in this architecture. Severity is "
                    + severity + ".";
        }

        String directText = String.join(", ", directlyAffected);

        if (indirectlyAffected.isEmpty()) {
            return authNode + " failed as an authentication or authorization dependency. Because it is a "
                    + nodeType
                    + ", direct consumers may lose login, token validation, session handling, or permission checks: "
                    + directText
                    + ". Severity is "
                    + severity
                    + ".";
        }

        String indirectText = String.join(", ", indirectlyAffected);

        return authNode + " failed as an authentication or authorization dependency. Direct consumers may lose login, token validation, session handling, or permission checks: "
                + directText
                + ". Downstream systems that may also be affected through protected access paths: "
                + indirectText
                + ". Severity is "
                + severity
                + ".";
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