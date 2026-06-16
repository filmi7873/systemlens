package com.reposcope.backend.model;

public class SystemNode {
    private final String id;
    private final String label;
    private final String type;

    public SystemNode(String id, String label, String type) {
        this.id = id;
        this.label = label;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getType() {
        return type;
    }
}