package com.reposcope.backend.model;

import java.util.List;

public class ImpactPath {
    private final List<String> nodes;

    public ImpactPath(List<String> nodes) {
        this.nodes = nodes;
    }

    public List<String> getNodes() {
        return nodes;
    }
}