# ADR 0001: Tech Stack Selection

## Status

Accepted

## Context

RepoScope is intended to demonstrate production-style full-stack engineering skills. The project needs a modern frontend, a backend suitable for enterprise-style software development, a relational database, and a way to visualize repository relationships.

## Decision

RepoScope will use:

- React and TypeScript for the frontend
- Spring Boot and Java for the backend
- PostgreSQL for persistence
- React Flow for dependency graph visualization
- GitHub API for repository metadata and file tree access

## Rationale

React and TypeScript are widely used for full-stack and frontend roles. TypeScript improves maintainability and makes the frontend codebase more reliable.

Spring Boot and Java are common in enterprise backend and full-stack roles. Using Spring Boot helps demonstrate backend API design, service-layer architecture, validation, testing, and database integration.

PostgreSQL provides a strong relational database foundation for modeling repositories, analyses, files, dependencies, and user history.

React Flow provides a polished way to display dependency graphs and architecture maps.

The GitHub API allows RepoScope to analyze real public repositories without requiring users to upload files manually.

## Alternatives Considered

### Rails

Rails would allow faster development, and I have prior experience with it. However, Spring Boot better supports my goal of strengthening my Java backend and enterprise full-stack profile.

### Node/Express

Node would pair naturally with a React frontend, but Spring Boot gives the project stronger backend credibility for Java-focused roles.

### MongoDB

A document database could store analysis results flexibly, but PostgreSQL is better for demonstrating relational modeling and querying.

## Consequences

This stack may take longer to set up than a Rails or Node project, but it better supports the goal of building a serious full-stack portfolio project for software engineering roles.