package com.reposcope.backend.dto;

public class AnalyzeRepositoryResponse {
    private String owner;
    private String repo;
    private String url;
    private String status;

    public AnalyzeRepositoryResponse(String owner, String repo, String url, String status) {
        this.owner = owner;
        this.repo = repo;
        this.url = url;
        this.status = status;
    }

    public String getOwner() {
        return owner;
    }

    public String getRepo() {
        return repo;
    }

    public String getUrl() {
        return url;
    }

    public String getStatus() {
        return status;
    }
}