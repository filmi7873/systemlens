package com.reposcope.backend.controller;

import com.reposcope.backend.dto.OutageSimulationRequest;
import com.reposcope.backend.dto.SimulationResultResponse;
import com.reposcope.backend.service.SimulationService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class SimulationController {

    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @GetMapping("/api/simulations/outage/sample")
    public SimulationResultResponse runSampleOutageSimulation() {
        return simulationService.runSamplePaymentOutage();
    }

    @GetMapping("/api/simulations/sample/nodes")
    public List<String> getSampleNodes() {
        return simulationService.getSampleNodes();
    }

    @PostMapping("/api/simulations/outage")
    public SimulationResultResponse runOutageSimulation(
            @RequestBody OutageSimulationRequest request
    ) {
        return simulationService.runOutageSimulation(request.getFailedNode());
    }
}