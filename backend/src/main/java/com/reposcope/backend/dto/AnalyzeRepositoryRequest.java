package com.reposcope.backend.dto;

public class AnalyzeRepositoryRequest {
    private String url;

    public AnalyzeRepositoryRequest() {
    }

    public AnalyzeRepositoryRequest(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}