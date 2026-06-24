package com.reposcope.backend.controller;

import com.reposcope.backend.dto.ArchitectureGraphResponse;
import com.reposcope.backend.dto.CustomOutageSimulationRequest;
import com.reposcope.backend.dto.CustomSchemaChangeSimulationRequest;
import com.reposcope.backend.dto.OutageSimulationRequest;
import com.reposcope.backend.dto.SchemaChangeSimulationRequest;
import com.reposcope.backend.dto.SimulationAnalysisResponse;
import com.reposcope.backend.dto.SimulationResultResponse;
import com.reposcope.backend.service.SimulationService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @GetMapping("/api/simulations/outage/sample")
    public SimulationResultResponse runSampleOutageSimulation() {
        return simulationService.runSamplePaymentOutage();
    }

    @PostMapping("/api/simulations/outage")
    public SimulationResultResponse runOutageSimulation(
            @RequestBody OutageSimulationRequest request
    ) {
        return simulationService.runOutageSimulation(request.getFailedNode());
    }

    @PostMapping("/api/simulations/schema-change")
    public SimulationResultResponse runSchemaChangeSimulation(
            @RequestBody SchemaChangeSimulationRequest request
    ) {
        return simulationService.runSchemaChangeSimulation(request.getChangedNode());
    }

    @PostMapping("/api/simulations/outage/custom")
    public SimulationResultResponse runCustomOutageSimulation(
            @RequestBody CustomOutageSimulationRequest request
    ) {
        return simulationService.runCustomOutageSimulation(request);
    }

    @PostMapping("/api/simulations/schema-change/custom")
    public SimulationResultResponse runCustomSchemaChangeSimulation(
            @RequestBody CustomSchemaChangeSimulationRequest request
    ) {
        return simulationService.runCustomSchemaChangeSimulation(request);
    }

    @PostMapping("/api/simulations/outage/analyze")
    public SimulationAnalysisResponse runOutageAnalysis(
            @RequestBody OutageSimulationRequest request
    ) {
        return simulationService.runOutageAnalysis(request.getFailedNode());
    }

    @PostMapping("/api/simulations/schema-change/analyze")
    public SimulationAnalysisResponse runSchemaChangeAnalysis(
            @RequestBody SchemaChangeSimulationRequest request
    ) {
        return simulationService.runSchemaChangeAnalysis(request.getChangedNode());
    }

    @PostMapping("/api/simulations/outage/custom/analyze")
    public SimulationAnalysisResponse runCustomOutageAnalysis(
            @RequestBody CustomOutageSimulationRequest request
    ) {
        return simulationService.runCustomOutageAnalysis(request);
    }

    @PostMapping("/api/simulations/schema-change/custom/analyze")
    public SimulationAnalysisResponse runCustomSchemaChangeAnalysis(
            @RequestBody CustomSchemaChangeSimulationRequest request
    ) {
        return simulationService.runCustomSchemaChangeAnalysis(request);
    }

    @GetMapping("/api/simulations/sample/nodes")
    public List<String> getSampleNodes() {
        return simulationService.getSampleNodes();
    }

    @GetMapping("/api/simulations/sample/graph")
    public ArchitectureGraphResponse getSampleArchitectureGraph() {
        return simulationService.getSampleArchitectureGraph();
    }
}