# RepoScope

RepoScope is a full-stack codebase intelligence platform that analyzes public GitHub repositories and generates architecture maps, dependency graphs, and onboarding guides for developers entering unfamiliar codebases.

## Problem

Developers often spend hours or days trying to understand how an unfamiliar repository is structured. RepoScope helps reduce that onboarding time by turning repository metadata, file structure, and dependency relationships into a readable dashboard.

## MVP

The MVP allows users to submit a public GitHub repository URL and receive:

- repository overview
- framework/language detection
- categorized file structure
- dependency graph
- onboarding guide
- complexity/risk report

## Tech Stack

- Frontend: React, TypeScript
- Backend: Spring Boot, Java
- Database: PostgreSQL
- Visualization: React Flow
- External API: GitHub API

## Status

In development.

## Roadmap

### Sprint 0: Project Setup

- Initialize monorepo
- Write PRD
- Create backlog
- Set up frontend and backend skeletons

### Sprint 1: GitHub Ingestion

- Parse GitHub repository URLs
- Fetch repository metadata
- Fetch repository file tree
- Store analysis results

### Sprint 2: Architecture Detection

- Detect React project structure
- Categorize files
- Generate architecture summary

### Sprint 3: Dependency Graph

- Parse imports
- Resolve file relationships
- Render interactive graph

### Sprint 4: Onboarding Report

- Generate onboarding guide
- Add complexity report
- Polish dashboard and case study