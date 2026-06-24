package com.reposcope.backend.engine;

import com.reposcope.backend.dto.SimulationResultResponse;
import com.reposcope.backend.model.ArchitectureGraph;
import com.reposcope.backend.sample.SampleArchitectureFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.reposcope.backend.model.SystemEdge;
import com.reposcope.backend.model.SystemNode;

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
void simulate_whenEmailQueueFails_impactsNotificationWorkerAndWebApp() {
    SimulationResultResponse result = outageSimulationEngine.simulate(graph, "Email Queue");

    assertEquals("Email Queue", result.getFailedNode());
    assertEquals("medium", result.getSeverity());

    assertEquals(List.of("Notification Worker"), result.getDirectlyAffected());
    assertEquals(List.of("Web App"), result.getIndirectlyAffected());

    assertTrue(result.getImpactPaths().contains(
            List.of("Email Queue", "Notification Worker")
    ));

    assertTrue(result.getImpactPaths().contains(
            List.of("Email Queue", "Notification Worker", "Web App")
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

    @Test
    void simulate_withCustomGraph_calculatesImpactUsingProvidedNodesAndEdges() {
        ArchitectureGraph customGraph = new ArchitectureGraph(
            List.of(
                    new SystemNode("web-app", "Web App", "frontend"),
                    new SystemNode("payments-api", "Payments API", "service"),
                    new SystemNode("checkout-service", "Checkout Service", "service")
            ),
            List.of(
                    new SystemEdge("edge-1", "Payments API", "Checkout Service", "supports"),
                    new SystemEdge("edge-2", "Checkout Service", "Web App", "supports")
            )
         );

        SimulationResultResponse result = outageSimulationEngine.simulate(
            customGraph,
            "Payments API"
    );

        assertEquals("Payments API", result.getFailedNode());
        assertEquals("medium", result.getSeverity());

        assertEquals(List.of("Checkout Service"), result.getDirectlyAffected());
        assertEquals(List.of("Web App"), result.getIndirectlyAffected());
        assertTrue(result.getUnaffected().isEmpty());

        assertTrue(result.getImpactPaths().contains(
            List.of("Payments API", "Checkout Service")
        ));

        assertTrue(result.getImpactPaths().contains(
            List.of("Payments API", "Checkout Service", "Web App")
        ));
   }
}