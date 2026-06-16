package com.reposcope.backend.service;

import com.reposcope.backend.dto.CustomOutageSimulationRequest;
import com.reposcope.backend.dto.ArchitectureGraphResponse;
import com.reposcope.backend.dto.SimulationResultResponse;
import com.reposcope.backend.engine.OutageSimulationEngine;
import com.reposcope.backend.engine.SchemaChangeSimulationEngine;
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
    private final SchemaChangeSimulationEngine schemaChangeSimulationEngine;

    public SimulationService(
            SampleArchitectureFactory sampleArchitectureFactory,
            OutageSimulationEngine outageSimulationEngine,
            SchemaChangeSimulationEngine schemaChangeSimulationEngine
    ) {
        this.sampleArchitectureFactory = sampleArchitectureFactory;
        this.outageSimulationEngine = outageSimulationEngine;
        this.schemaChangeSimulationEngine = schemaChangeSimulationEngine;
    }

    public SimulationResultResponse runSamplePaymentOutage() {
        return runOutageSimulation("Payment Provider");
    }

    public SimulationResultResponse runOutageSimulation(String failedNode) {
        ArchitectureGraph graph = sampleArchitectureFactory.createEcommerceArchitecture();
        return outageSimulationEngine.simulate(graph, failedNode);
    }

    public SimulationResultResponse runSchemaChangeSimulation(String changedNode) {
        ArchitectureGraph graph = sampleArchitectureFactory.createEcommerceArchitecture();
        return schemaChangeSimulationEngine.simulate(graph, changedNode);
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

    public SimulationResultResponse runCustomOutageSimulation(
        CustomOutageSimulationRequest request
    ) {
         List<SystemNode> nodes = request.getNodes()
            .stream()
            .map(node -> new SystemNode(
                    node.getId(),
                    node.getLabel(),
                    node.getType()
            ))
            .toList();

         List<SystemEdge> edges = request.getEdges()
            .stream()
            .map(edge -> new SystemEdge(
                    edge.getId(),
                    edge.getSourceNode(),
                    edge.getTargetNode(),
                    edge.getRelationship()
            ))
            .toList();

        ArchitectureGraph graph = new ArchitectureGraph(nodes, edges);

        return outageSimulationEngine.simulate(graph, request.getFailedNode());
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