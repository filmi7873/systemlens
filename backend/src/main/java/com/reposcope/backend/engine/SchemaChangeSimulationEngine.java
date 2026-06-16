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
public class SchemaChangeSimulationEngine {

    public SimulationResultResponse simulate(ArchitectureGraph graph, String changedNode) {
        validateInput(graph, changedNode);

        Map<String, List<String>> dependents = graph.buildDependentsMap();

        List<String> directlyAffected = findDirectlyAffectedSystems(
                graph,
                changedNode,
                dependents
        );

        Set<String> indirectlyAffectedSet = new LinkedHashSet<>();

        for (String directNode : directlyAffected) {
            collectDownstreamImpact(directNode, dependents, indirectlyAffectedSet);
        }

        indirectlyAffectedSet.removeAll(directlyAffected);
        indirectlyAffectedSet.remove(changedNode);

        List<String> indirectlyAffected = new ArrayList<>(indirectlyAffectedSet);

        List<String> impacted = new ArrayList<>();
        impacted.add(changedNode);
        impacted.addAll(directlyAffected);
        impacted.addAll(indirectlyAffected);

        List<String> unaffected = graph.getNodeLabels().stream()
                .filter(node -> !impacted.contains(node))
                .toList();

        List<List<String>> impactPaths = buildImpactPaths(
                changedNode,
                directlyAffected,
                dependents
        );

        String severity = calculateSeverity(
                graph,
                changedNode,
                directlyAffected,
                indirectlyAffected
        );

        String explanation = buildExplanation(
                graph,
                changedNode,
                directlyAffected,
                indirectlyAffected,
                severity
        );

        return new SimulationResultResponse(
                changedNode,
                severity,
                directlyAffected,
                indirectlyAffected,
                unaffected,
                impactPaths,
                explanation
        );
    }

    private void validateInput(ArchitectureGraph graph, String changedNode) {
        if (changedNode == null || changedNode.isBlank()) {
            throw new IllegalArgumentException("Changed node is required.");
        }

        if (!graph.containsNode(changedNode)) {
            throw new IllegalArgumentException("Unknown node: " + changedNode);
        }
    }

    private List<String> findDirectlyAffectedSystems(
            ArchitectureGraph graph,
            String changedNode,
            Map<String, List<String>> dependents
    ) {
        String changedNodeType = getNodeType(graph, changedNode);

        if (changedNodeType.equals("frontend")) {
            return List.of();
        }

        return dependents.getOrDefault(changedNode, List.of());
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
            String changedNode,
            List<String> directlyAffected,
            Map<String, List<String>> dependents
    ) {
        List<List<String>> paths = new ArrayList<>();

        for (String directNode : directlyAffected) {
            List<String> currentPath = new ArrayList<>();
            currentPath.add(changedNode);
            currentPath.add(directNode);

            paths.add(currentPath);
            collectImpactPaths(directNode, dependents, currentPath, paths);
        }

        return paths;
    }

    private void collectImpactPaths(
            String currentNode,
            Map<String, List<String>> dependents,
            List<String> currentPath,
            List<List<String>> paths
    ) {
        List<String> nextDependents = dependents.getOrDefault(currentNode, List.of());

        for (String dependent : nextDependents) {
            List<String> newPath = new ArrayList<>(currentPath);
            newPath.add(dependent);
            paths.add(newPath);

            collectImpactPaths(dependent, dependents, newPath, paths);
        }
    }

    private String calculateSeverity(
            ArchitectureGraph graph,
            String changedNode,
            List<String> directlyAffected,
            List<String> indirectlyAffected
    ) {
        String changedNodeType = getNodeType(graph, changedNode);
        int impactCount = directlyAffected.size() + indirectlyAffected.size();

        if (changedNodeType.equals("database") && impactCount >= 2) {
            return "high";
        }

        if (changedNodeType.equals("external") && impactCount >= 2) {
            return "high";
        }

        if (impactCount >= 3) {
            return "high";
        }

        if (impactCount >= 1) {
            return "medium";
        }

        return "low";
    }

    private String buildExplanation(
            ArchitectureGraph graph,
            String changedNode,
            List<String> directlyAffected,
            List<String> indirectlyAffected,
            String severity
    ) {
        String changedNodeType = getNodeType(graph, changedNode);

        if (directlyAffected.isEmpty()) {
            return changedNode + " changed, but no downstream systems consume it in this architecture. Severity is "
                    + severity + ".";
        }

        String directText = String.join(", ", directlyAffected);

        if (indirectlyAffected.isEmpty()) {
            return changedNode + " changed. Because it is a " + changedNodeType
                    + ", its contract may affect direct consumers: " + directText
                    + ". Severity is " + severity + ".";
        }

        String indirectText = String.join(", ", indirectlyAffected);

        return changedNode + " changed. Because it is a " + changedNodeType
                + ", its schema or contract may affect direct consumers: " + directText
                + ". Downstream systems that may also need validation: " + indirectText
                + ". Severity is " + severity + ".";
    }

    private String getNodeType(ArchitectureGraph graph, String nodeLabel) {
        return graph.getNodes()
                .stream()
                .filter(node -> node.getLabel().equals(nodeLabel))
                .map(SystemNode::getType)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown node: " + nodeLabel));
    }
}