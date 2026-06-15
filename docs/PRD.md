# SystemLens Product Requirements Document

## 1. Overview

SystemLens is a full-stack architecture simulation platform that helps developers understand how failures, changes, and traffic spikes move through software systems.

A user can model a software architecture as a graph of services, databases, queues, frontend clients, and external APIs. SystemLens can then simulate events such as service outages, database schema changes, and traffic spikes to show downstream impact.

## 2. Problem

Software systems often fail in connected ways. A single unavailable service, database change, queue backup, or external API outage can affect many other parts of the system.

Developers, especially junior engineers, often learn system behavior only after something breaks. SystemLens provides a visual and interactive way to reason about architecture risk before production.

## 3. Target Users

Primary users:

- junior developers learning system design
- full-stack developers working on distributed systems
- engineering teams reviewing architecture changes
- developers preparing for incident response or design reviews

## 4. MVP Scope

The MVP will support a sample e-commerce architecture and an outage simulation.

The user can:

- view a predefined architecture
- select or trigger a service outage
- see directly affected systems
- see indirectly affected systems
- view impact severity
- view a plain-English explanation of the failure path

## 5. Non-Goals for MVP

The MVP will not include:

- real production monitoring
- live tracing
- Kubernetes integration
- automatic infrastructure discovery
- authentication
- team collaboration
- AI-generated architecture diagrams
- custom user-created systems initially

## 6. Success Criteria

SystemLens MVP is successful when:

- the frontend can call a backend simulation endpoint
- the backend can represent a system as nodes and dependencies
- the backend can run a graph traversal from a failed node
- the response identifies direct and indirect impact
- the frontend displays affected systems clearly
- the demo tells a strong story in under one minute

## 7. Sample Architecture

The first sample architecture will model a simple e-commerce system:

- Web App
- Auth Service
- Product Service
- Inventory Service
- Cart Service
- Checkout Service
- Payment Provider
- Order Database
- Inventory Database
- Email Queue
- Notification Worker

## 8. Core Simulation Types

### Outage Simulation

A node becomes unavailable. SystemLens calculates what depends on it and how impact spreads.

### Schema Change Simulation

A database or API contract changes. SystemLens identifies dependent services that may break.

### Traffic Spike Simulation

A node receives increased traffic. SystemLens identifies likely bottlenecks and overloaded downstream dependencies.

## 9. Initial MVP Simulation

The first simulation will be a hardcoded outage simulation.

Example:

Payment Provider fails.

SystemLens should report:

- Checkout Service directly impacted
- Web App indirectly impacted
- Order Database not directly impacted
- Email Queue unaffected
- Severity: high
- Explanation: Checkout depends on Payment Provider, and the Web App depends on Checkout for purchase completion.

## 10. Technical Requirements

Frontend:

- React
- TypeScript
- API client layer
- simulation result display
- React Flow later for graph visualization

Backend:

- Java
- Spring Boot
- REST API
- graph traversal logic
- simulation service layer

Testing:

- backend tests for graph traversal
- backend tests for simulation output
- frontend tests later for core flows

## 11. Risks

Potential risks:

- scope creep
- graph logic becoming too complex too early
- UI complexity
- unclear simulation rules
- trying to support too many system types too soon

## 12. Risk Mitigation

- start with one sample architecture
- start with one outage simulation
- use simple graph traversal first
- add visual graph only after API works
- keep every simulation explainable