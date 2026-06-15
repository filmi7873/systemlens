package com.reposcope.backend.service;

import com.reposcope.backend.dto.AnalyzeRepositoryResponse;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class RepositoryAnalysisService {

    public AnalyzeRepositoryResponse analyzeRepository(String url) {
        ParsedGitHubUrl parsedUrl = parseGitHubUrl(url);

        return new AnalyzeRepositoryResponse(
                parsedUrl.owner(),
                parsedUrl.repo(),
                url,
                "parsed"
        );
    }

    private ParsedGitHubUrl parseGitHubUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Repository URL is required.");
        }

        URI uri = URI.create(url);

        String host = uri.getHost();
        if (host == null || !host.equalsIgnoreCase("github.com")) {
            throw new IllegalArgumentException("Only github.com repository URLs are supported.");
        }

        String path = uri.getPath();

        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Invalid GitHub repository URL.");
        }

        String[] parts = path.split("/");

        if (parts.length < 3 || parts[1].isBlank() || parts[2].isBlank()) {
            throw new IllegalArgumentException("GitHub URL must include an owner and repository name.");
        }

        String owner = parts[1];
        String repo = parts[2].replace(".git", "");

        return new ParsedGitHubUrl(owner, repo);
    }

    private record ParsedGitHubUrl(String owner, String repo) {
    }
}