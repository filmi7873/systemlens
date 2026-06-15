# SystemLens

SystemLens is a full-stack architecture simulation platform that lets developers model software systems and simulate outages, schema changes, and traffic spikes to understand downstream impact before production.

## Problem

Modern software systems depend on many moving parts: frontend clients, APIs, databases, queues, workers, caches, and external services. When one part fails or changes, it can be difficult to quickly understand what else is affected.

SystemLens helps developers reason about system behavior by modeling architecture as a dependency graph and running simulations against that graph.

## MVP

The MVP allows users to:

- view a sample software architecture graph
- simulate a service outage
- see directly and indirectly affected systems
- view severity and impact explanations
- eventually create custom architecture maps

## Tech Stack

- Frontend: React, TypeScript
- Backend: Spring Boot, Java
- Database: PostgreSQL later
- Visualization: React Flow later
- Simulation Model: Graph traversal and impact scoring

## Status

In development.

## Roadmap

### Sprint 0: Project Pivot

- Rename product from RepoScope to SystemLens
- Update documentation
- Keep existing React/Spring Boot foundation
- Define MVP simulation model

### Sprint 1: Sample Simulation API

- Create backend model for system nodes and dependencies
- Create outage simulation endpoint
- Return affected nodes and impact severity
- Display simulation results in React

### Sprint 2: Interactive Architecture UI

- Display system nodes and relationships
- Add sample architecture templates
- Allow users to select a failure point

### Sprint 3: Graph Visualization

- Integrate React Flow
- Visualize affected systems
- Highlight direct and indirect impact

### Sprint 4: Custom Systems

- Let users create nodes
- Let users connect dependencies
- Persist systems in database

### Sprint 5: Advanced Simulations

- Schema change impact
- Traffic spike impact
- Risk scoring
- mitigation recommendations