package com.reposcope.backend.service;

import com.reposcope.backend.dto.ArchitectureGraphResponse;
import com.reposcope.backend.dto.CustomAuthFailureSimulationRequest;
import com.reposcope.backend.dto.CustomOutageSimulationRequest;
import com.reposcope.backend.dto.CustomSchemaChangeSimulationRequest;
import com.reposcope.backend.dto.RiskAssessmentResponse;
import com.reposcope.backend.dto.SimulationAnalysisResponse;
import com.reposcope.backend.dto.SimulationResultResponse;
import com.reposcope.backend.engine.AuthFailureSimulationEngine;
import com.reposcope.backend.engine.OutageSimulationEngine;
import com.reposcope.backend.engine.RiskAssessmentEngine;
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
    private final AuthFailureSimulationEngine authFailureSimulationEngine;
    private final RiskAssessmentEngine riskAssessmentEngine;

    public SimulationService(
            SampleArchitectureFactory sampleArchitectureFactory,
            OutageSimulationEngine outageSimulationEngine,
            SchemaChangeSimulationEngine schemaChangeSimulationEngine,
            AuthFailureSimulationEngine authFailureSimulationEngine,
            RiskAssessmentEngine riskAssessmentEngine
    ) {
        this.sampleArchitectureFactory = sampleArchitectureFactory;
        this.outageSimulationEngine = outageSimulationEngine;
        this.schemaChangeSimulationEngine = schemaChangeSimulationEngine;
        this.authFailureSimulationEngine = authFailureSimulationEngine;
        this.riskAssessmentEngine = riskAssessmentEngine;
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

    public SimulationResultResponse runCustomOutageSimulation(
            CustomOutageSimulationRequest request
    ) {
        ArchitectureGraph graph = buildCustomOutageGraph(request);
        return outageSimulationEngine.simulate(graph, request.getFailedNode());
    }

    public SimulationResultResponse runCustomSchemaChangeSimulation(
            CustomSchemaChangeSimulationRequest request
    ) {
        ArchitectureGraph graph = buildCustomSchemaChangeGraph(request);
        return schemaChangeSimulationEngine.simulate(graph, request.getChangedNode());
    }

    public SimulationResultResponse runCustomAuthFailureSimulation(
            CustomAuthFailureSimulationRequest request
    ) {
        ArchitectureGraph graph = buildCustomAuthFailureGraph(request);
        return authFailureSimulationEngine.simulate(graph, request.getAuthNode());
    }

    public SimulationAnalysisResponse runOutageAnalysis(String failedNode) {
        ArchitectureGraph graph = sampleArchitectureFactory.createEcommerceArchitecture();

        SimulationResultResponse simulation = outageSimulationEngine.simulate(
                graph,
                failedNode
        );

        RiskAssessmentResponse riskAssessment = riskAssessmentEngine.assess(
                graph,
                simulation,
                "outage"
        );

        return new SimulationAnalysisResponse(simulation, riskAssessment);
    }

    public SimulationAnalysisResponse runSchemaChangeAnalysis(String changedNode) {
        ArchitectureGraph graph = sampleArchitectureFactory.createEcommerceArchitecture();

        SimulationResultResponse simulation = schemaChangeSimulationEngine.simulate(
                graph,
                changedNode
        );

        RiskAssessmentResponse riskAssessment = riskAssessmentEngine.assess(
                graph,
                simulation,
                "schema-change"
        );

        return new SimulationAnalysisResponse(simulation, riskAssessment);
    }

    public SimulationAnalysisResponse runCustomOutageAnalysis(
            CustomOutageSimulationRequest request
    ) {
        ArchitectureGraph graph = buildCustomOutageGraph(request);

        SimulationResultResponse simulation = outageSimulationEngine.simulate(
                graph,
                request.getFailedNode()
        );

        RiskAssessmentResponse riskAssessment = riskAssessmentEngine.assess(
                graph,
                simulation,
                "outage"
        );

        return new SimulationAnalysisResponse(simulation, riskAssessment);
    }

    public SimulationAnalysisResponse runCustomSchemaChangeAnalysis(
            CustomSchemaChangeSimulationRequest request
    ) {
        ArchitectureGraph graph = buildCustomSchemaChangeGraph(request);

        SimulationResultResponse simulation = schemaChangeSimulationEngine.simulate(
                graph,
                request.getChangedNode()
        );

        RiskAssessmentResponse riskAssessment = riskAssessmentEngine.assess(
                graph,
                simulation,
                "schema-change"
        );

        return new SimulationAnalysisResponse(simulation, riskAssessment);
    }

    public SimulationAnalysisResponse runCustomAuthFailureAnalysis(
            CustomAuthFailureSimulationRequest request
    ) {
        ArchitectureGraph graph = buildCustomAuthFailureGraph(request);

        SimulationResultResponse simulation = authFailureSimulationEngine.simulate(
                graph,
                request.getAuthNode()
        );

        RiskAssessmentResponse riskAssessment = riskAssessmentEngine.assess(
                graph,
                simulation,
                "auth-failure"
        );

        return new SimulationAnalysisResponse(simulation, riskAssessment);
    }

    public List<String> getSampleNodes() {
        ArchitectureGraph graph = sampleArchitectureFactory.createEcommerceArchitecture();
        return graph.getNodeLabels();
    }

    public ArchitectureGraphResponse getSampleArchitectureGraph() {
        ArchitectureGraph graph = sampleArchitectureFactory.createEcommerceArchitecture();

        List<ArchitectureGraphResponse.ArchitectureNodeResponse> nodes = graph.getNodes()
                .stream()
                .map(this::toArchitectureNodeResponse)
                .toList();

        List<ArchitectureGraphResponse.ArchitectureEdgeResponse> edges = graph.getEdges()
                .stream()
                .map(this::toArchitectureEdgeResponse)
                .toList();

        return new ArchitectureGraphResponse(nodes, edges);
    }

    private ArchitectureGraph buildCustomOutageGraph(
            CustomOutageSimulationRequest request
    ) {
        return buildCustomGraph(
                request.getNodes()
                        .stream()
                        .map(node -> new SystemNode(
                                node.getId(),
                                node.getLabel(),
                                node.getType(),
                                node.isContainsPii(),
                                node.getDataSensitivity(),
                                node.getComplianceTags()
                        ))
                        .toList(),
                request.getEdges()
                        .stream()
                        .map(edge -> new SystemEdge(
                                edge.getId(),
                                edge.getSourceNode(),
                                edge.getTargetNode(),
                                edge.getRelationship()
                        ))
                        .toList()
        );
    }

    private ArchitectureGraph buildCustomSchemaChangeGraph(
            CustomSchemaChangeSimulationRequest request
    ) {
        return buildCustomGraph(
                request.getNodes()
                        .stream()
                        .map(node -> new SystemNode(
                                node.getId(),
                                node.getLabel(),
                                node.getType(),
                                node.isContainsPii(),
                                node.getDataSensitivity(),
                                node.getComplianceTags()
                        ))
                        .toList(),
                request.getEdges()
                        .stream()
                        .map(edge -> new SystemEdge(
                                edge.getId(),
                                edge.getSourceNode(),
                                edge.getTargetNode(),
                                edge.getRelationship()
                        ))
                        .toList()
        );
    }

    private ArchitectureGraph buildCustomAuthFailureGraph(
            CustomAuthFailureSimulationRequest request
    ) {
        return buildCustomGraph(
                request.getNodes()
                        .stream()
                        .map(node -> new SystemNode(
                                node.getId(),
                                node.getLabel(),
                                node.getType(),
                                node.isContainsPii(),
                                node.getDataSensitivity(),
                                node.getComplianceTags()
                        ))
                        .toList(),
                request.getEdges()
                        .stream()
                        .map(edge -> new SystemEdge(
                                edge.getId(),
                                edge.getSourceNode(),
                                edge.getTargetNode(),
                                edge.getRelationship()
                        ))
                        .toList()
        );
    }

    private ArchitectureGraph buildCustomGraph(
            List<SystemNode> nodes,
            List<SystemEdge> edges
    ) {
        return new ArchitectureGraph(nodes, edges);
    }

    private ArchitectureGraphResponse.ArchitectureNodeResponse toArchitectureNodeResponse(
            SystemNode node
    ) {
        return new ArchitectureGraphResponse.ArchitectureNodeResponse(
                node.getId(),
                node.getLabel(),
                node.getType(),
                node.isContainsPii(),
                node.getDataSensitivity(),
                node.getComplianceTags()
        );
    }

    private ArchitectureGraphResponse.ArchitectureEdgeResponse toArchitectureEdgeResponse(
            SystemEdge edge
    ) {
        return new ArchitectureGraphResponse.ArchitectureEdgeResponse(
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