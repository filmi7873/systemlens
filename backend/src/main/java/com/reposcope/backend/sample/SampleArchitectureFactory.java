package com.reposcope.backend.sample;

import com.reposcope.backend.model.ArchitectureGraph;
import com.reposcope.backend.model.SystemEdge;
import com.reposcope.backend.model.SystemNode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SampleArchitectureFactory {

    public ArchitectureGraph createEcommerceArchitecture() {
        List<SystemNode> nodes = List.of(
                new SystemNode("web-app", "Web App", "frontend"),
                new SystemNode("auth-service", "Auth Service", "service"),
                new SystemNode("product-service", "Product Service", "service"),
                new SystemNode("inventory-service", "Inventory Service", "service"),
                new SystemNode("cart-service", "Cart Service", "service"),
                new SystemNode("checkout-service", "Checkout Service", "service"),
                new SystemNode("payment-provider", "Payment Provider", "external"),
                new SystemNode("order-database", "Order Database", "database"),
                new SystemNode("inventory-database", "Inventory Database", "database"),
                new SystemNode("email-queue", "Email Queue", "queue"),
                new SystemNode("notification-worker", "Notification Worker", "worker")
        );

        List<SystemEdge> edges = List.of(
                new SystemEdge("edge-auth-web", "Auth Service", "Web App", "supports"),
                new SystemEdge("edge-product-web", "Product Service", "Web App", "supports"),
                new SystemEdge("edge-inventory-web", "Inventory Service", "Web App", "supports"),

                new SystemEdge("edge-cart-checkout", "Cart Service", "Checkout Service", "supports"),
                new SystemEdge("edge-inventory-checkout", "Inventory Service", "Checkout Service", "supports"),
                new SystemEdge("edge-checkout-web", "Checkout Service", "Web App", "supports"),

                new SystemEdge("edge-payment-checkout", "Payment Provider", "Checkout Service", "supports"),
                new SystemEdge("edge-order-checkout", "Order Database", "Checkout Service", "supports"),
                new SystemEdge("edge-inventory-db-service", "Inventory Database", "Inventory Service", "supports"),

                new SystemEdge("edge-email-worker", "Email Queue", "Notification Worker", "supports"),
                new SystemEdge("edge-notification-web", "Notification Worker", "Web App", "supports")
        );

        return new ArchitectureGraph(nodes, edges);
    }
}