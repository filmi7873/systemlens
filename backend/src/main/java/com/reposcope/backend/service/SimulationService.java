package com.reposcope.backend.service;

import com.reposcope.backend.dto.ArchitectureGraphResponse;
import com.reposcope.backend.dto.SimulationResultResponse;
import com.reposcope.backend.engine.OutageSimulationEngine;
import com.reposcope.backend.model.ArchitectureGraph;
import com.reposcope.backend.model.SystemEdge;
import com.reposcope.backend.model.SystemNode;
import com.reposcope.backend.sample.SampleArchitectureFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SimulationService {

    private final SampleArchitectureFactory sampleArchitectureFactory;
    private final OutageSimulationEngine outageSimulationEngine;

    public SimulationService(
            SampleArchitectureFactory sampleArchitectureFactory,
            OutageSimulationEngine outageSimulationEngine
    ) {
        this.sampleArchitectureFactory = sampleArchitectureFactory;
        this.outageSimulationEngine = outageSimulationEngine;
    }

    public SimulationResultResponse runSamplePaymentOutage() {
        return runOutageSimulation("Payment Provider");
    }

    public SimulationResultResponse runOutageSimulation(String failedNode) {
        ArchitectureGraph graph = sampleArchitectureFactory.createEcommerceArchitecture();
        return outageSimulationEngine.simulate(graph, failedNode);
    }

    public List<String> getSampleNodes() {
        ArchitectureGraph graph = sampleArchitectureFactory.createEcommerceArchitecture();
        return graph.getNodeLabels();
    }

    public ArchitectureGraphResponse getSampleArchitectureGraph() {
        ArchitectureGraph graph = sampleArchitectureFactory.createEcommerceArchitecture();

        List<ArchitectureGraphResponse.ArchitectureNode> nodes = graph.getNodes()
                .stream()
                .map(this::toArchitectureNodeResponse)
                .toList();

        List<ArchitectureGraphResponse.ArchitectureEdge> edges = graph.getEdges()
                .stream()
                .map(this::toArchitectureEdgeResponse)
                .toList();

        return new ArchitectureGraphResponse(nodes, edges);
    }

    private ArchitectureGraphResponse.ArchitectureNode toArchitectureNodeResponse(SystemNode node) {
        return new ArchitectureGraphResponse.ArchitectureNode(
                node.getId(),
                node.getLabel(),
                node.getType()
        );
    }

    private ArchitectureGraphResponse.ArchitectureEdge toArchitectureEdgeResponse(SystemEdge edge) {
        return new ArchitectureGraphResponse.ArchitectureEdge(
                edge.getId(),
                normalizeNodeId(edge.getSourceNode()),
                normalizeNodeId(edge.getTargetNode()),
                edge.getRelationship()
        );
    }

    private String normalizeNodeId(String nodeLabel) {
        return nodeLabel.toLowerCase().replace(" ", "-");
    }
}