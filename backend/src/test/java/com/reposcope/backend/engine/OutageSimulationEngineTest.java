package com.reposcope.backend.engine;

import com.reposcope.backend.dto.SimulationResultResponse;
import com.reposcope.backend.model.ArchitectureGraph;
import com.reposcope.backend.sample.SampleArchitectureFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OutageSimulationEngineTest {

    private OutageSimulationEngine outageSimulationEngine;
    private ArchitectureGraph graph;

    @BeforeEach
    void setUp() {
        outageSimulationEngine = new OutageSimulationEngine();

        SampleArchitectureFactory sampleArchitectureFactory = new SampleArchitectureFactory();
        graph = sampleArchitectureFactory.createEcommerceArchitecture();
    }

    @Test
    void simulate_whenPaymentProviderFails_impactsCheckoutAndWebApp() {
        SimulationResultResponse result = outageSimulationEngine.simulate(graph, "Payment Provider");

        assertEquals("Payment Provider", result.getFailedNode());
        assertEquals("medium", result.getSeverity());

        assertTrue(result.getDirectlyAffected().contains("Checkout Service"));
        assertTrue(result.getIndirectlyAffected().contains("Web App"));

        assertTrue(result.getImpactPaths().contains(
                List.of("Payment Provider", "Checkout Service")
        ));

        assertTrue(result.getImpactPaths().contains(
                List.of("Payment Provider", "Checkout Service", "Web App")
        ));
    }

    @Test
    void simulate_whenInventoryDatabaseFails_impactsInventoryCheckoutAndWebApp() {
        SimulationResultResponse result = outageSimulationEngine.simulate(graph, "Inventory Database");

        assertEquals("Inventory Database", result.getFailedNode());
        assertEquals("medium", result.getSeverity());

        assertTrue(result.getDirectlyAffected().contains("Inventory Service"));
        assertTrue(result.getIndirectlyAffected().contains("Checkout Service"));
        assertTrue(result.getIndirectlyAffected().contains("Web App"));

        assertTrue(result.getImpactPaths().contains(
                List.of("Inventory Database", "Inventory Service")
        ));

        assertTrue(result.getImpactPaths().contains(
                List.of("Inventory Database", "Inventory Service", "Checkout Service")
        ));

        assertTrue(result.getImpactPaths().contains(
                List.of("Inventory Database", "Inventory Service", "Checkout Service", "Web App")
        ));

        assertTrue(result.getImpactPaths().contains(
                List.of("Inventory Database", "Inventory Service", "Web App")
        ));
    }

    @Test
    void simulate_whenEmailQueueFails_impactsNotificationWorkerOnly() {
        SimulationResultResponse result = outageSimulationEngine.simulate(graph, "Email Queue");

        assertEquals("Email Queue", result.getFailedNode());
        assertEquals("low", result.getSeverity());

        assertTrue(result.getDirectlyAffected().contains("Notification Worker"));
        assertTrue(result.getIndirectlyAffected().isEmpty());

        assertTrue(result.getImpactPaths().contains(
                List.of("Email Queue", "Notification Worker")
        ));
    }

    @Test
    void simulate_whenUnknownNodeProvided_throwsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> outageSimulationEngine.simulate(graph, "Fake Service")
        );

        assertEquals("Unknown node: Fake Service", exception.getMessage());
    }

    @Test
    void simulate_whenFailedNodeIsBlank_throwsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> outageSimulationEngine.simulate(graph, "")
        );

        assertEquals("Failed node is required.", exception.getMessage());
    }
}