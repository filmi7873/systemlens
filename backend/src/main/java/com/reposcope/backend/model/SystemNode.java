package com.reposcope.backend.model;

import java.util.List;

public class SystemNode {
    private String id;
    private String label;
    private String type;

    private boolean containsPii;
    private String dataSensitivity;
    private List<String> complianceTags;

    public SystemNode() {
        this.containsPii = false;
        this.dataSensitivity = "none";
        this.complianceTags = List.of();
    }

    public SystemNode(String id, String label, String type) {
        this.id = id;
        this.label = label;
        this.type = type;
        this.containsPii = false;
        this.dataSensitivity = "none";
        this.complianceTags = List.of();
    }

    public SystemNode(
            String id,
            String label,
            String type,
            boolean containsPii,
            String dataSensitivity,
            List<String> complianceTags
    ) {
        this.id = id;
        this.label = label;
        this.type = type;
        this.containsPii = containsPii;
        this.dataSensitivity = dataSensitivity == null ? "none" : dataSensitivity;
        this.complianceTags = complianceTags == null ? List.of() : complianceTags;
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

    public boolean isContainsPii() {
        return containsPii;
    }

    public String getDataSensitivity() {
        return dataSensitivity == null ? "none" : dataSensitivity;
    }

    public List<String> getComplianceTags() {
        return complianceTags == null ? List.of() : complianceTags;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setContainsPii(boolean containsPii) {
        this.containsPii = containsPii;
    }

    public void setDataSensitivity(String dataSensitivity) {
        this.dataSensitivity = dataSensitivity == null ? "none" : dataSensitivity;
    }

    public void setComplianceTags(List<String> complianceTags) {
        this.complianceTags = complianceTags == null ? List.of() : complianceTags;
    }
}