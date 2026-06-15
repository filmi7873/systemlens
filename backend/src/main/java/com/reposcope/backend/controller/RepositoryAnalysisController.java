package com.reposcope.backend.controller;

import com.reposcope.backend.dto.AnalyzeRepositoryRequest;
import com.reposcope.backend.dto.AnalyzeRepositoryResponse;
import com.reposcope.backend.service.RepositoryAnalysisService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class RepositoryAnalysisController {

    private final RepositoryAnalysisService repositoryAnalysisService;

    public RepositoryAnalysisController(RepositoryAnalysisService repositoryAnalysisService) {
        this.repositoryAnalysisService = repositoryAnalysisService;
    }

    @PostMapping("/api/repositories/analyze")
    public AnalyzeRepositoryResponse analyzeRepository(@RequestBody AnalyzeRepositoryRequest request) {
        return repositoryAnalysisService.analyzeRepository(request.getUrl());
    }
}