# SystemLens

**SystemLens** is a full-stack architecture impact simulator that helps developers understand how outages, schema changes, and authentication failures propagate through software systems.

Users can model an architecture as a dependency graph, run simulations, visualize downstream blast radius, and generate risk reports with recommended validation steps before deploying, recovering, or rolling back a system.

## Live Demo

Frontend: `https://systemlens.vercel.app`  
Backend API: `https://systemlens-backend.onrender.com`

## Demo

### Custom Architecture Builder

Add systems, define dependencies, and model an architecture as a directed graph.

### Blast Radius Visualization

Visualize direct, indirect, and unaffected systems on a live React Flow graph.

### Risk Assessment Report

Generate risk scores, risk factors, impact paths, and validation recommendations based on the simulation result.

## Why I Built This

Modern software systems are interconnected. A database outage, external provider failure, authentication issue, or backend contract change can affect services and customer-facing flows in ways that are not always obvious.

SystemLens was built to make those dependencies visible.

The goal is to help developers answer questions like:

- What systems are directly affected if this component fails?
- What systems are indirectly affected through dependency chains?
- Does this change reach a customer-facing surface?
- Does this failure touch authentication, payment, checkout, or regulated-data workflows?
- What should be validated before deploying, recovering, or rolling back?

## Features

- Build custom architecture dependency graphs
- Save and load custom architectures locally
- Add systems such as services, databases, frontends, queues, workers, and external providers
- Define directional dependencies between systems
- Run outage simulations
- Run schema-change simulations
- Run authentication-failure simulations
- Filter auth-failure simulations to authentication-related nodes
- Tag systems that contain PII
- Assign data sensitivity levels such as internal, confidential, and restricted
- Add compliance tags such as PII, PCI, HIPAA, SOC2, and GDPR
- Visualize direct and indirect blast radius on a live graph
- Generate impact paths through the architecture
- Identify unaffected systems
- Calculate deployment risk level and risk score
- Generate risk factors and validation recommendations
- Load a starter architecture for quick demos
- Deploy frontend and backend separately using Vercel and Render

## Tech Stack

### Frontend

- React
- TypeScript
- Vite
- React Flow / XYFlow
- CSS

### Backend

- Java
- Spring Boot
- Maven
- REST API
- Docker

### Deployment

- Vercel for the frontend
- Render for the backend
- Environment-based API configuration
- CORS configuration for deployed frontend/backend communication

## How It Works

SystemLens represents software architecture as a directed graph.

Each node represents a system component, such as a service, database, frontend, queue, worker, or external provider.

Each edge represents a dependency relationship:

```text
Source system → Dependent system
```

If the source system fails, changes, or becomes unavailable as an authentication dependency, the dependent system may be affected.

When a simulation runs, the backend traverses the graph to calculate:

- Directly affected systems
- Indirectly affected systems
- Unaffected systems
- Impact paths
- Severity
- Deployment or operational risk assessment
- Recommended validation steps

## Example Architecture

The starter graph includes multiple dependency paths:

```text
User Database → Auth Service → API Gateway → Web App

Payment Provider → Checkout Service → Web App

Order Database → Checkout Service → Web App
```

Example outage:

```text
Order Database fails
```

SystemLens identifies:

```text
Directly affected:
Checkout Service

Indirectly affected:
Web App

Unaffected:
User Database, Auth Service, API Gateway, Payment Provider
```

Example auth failure:

```text
Auth Service fails as an authentication dependency
```

SystemLens identifies:

```text
Directly affected:
API Gateway

Indirectly affected:
Web App

Unaffected:
User Database, Payment Provider, Checkout Service, Order Database
```

## Risk Assessment

SystemLens generates a deployment readiness or recovery report after each simulation.

The risk assessment considers factors such as:

- Number of affected downstream systems
- Length of the longest impact path
- Whether the impact reaches a frontend or customer-facing surface
- Whether checkout, payment, order, or cart-related flows are affected
- Whether authentication, session, token, or permission paths are affected
- Whether the source is a database or external provider
- Whether affected systems contain PII
- Whether affected systems are marked as confidential or restricted
- Whether affected systems are tagged with compliance scopes such as PCI, HIPAA, SOC2, GDPR, or PII
- Whether the simulation is an outage, schema-change, or auth-failure scenario

Example recommendations include:

- Verify database recovery steps
- Validate indirect consumers
- Run customer-facing smoke tests
- Smoke test checkout and payment flows
- Test login, logout, token refresh, expired-session handling, and protected-route access behavior
- Confirm role-based permissions remain enforced during auth dependency failure
- Run integration tests for changed contracts
- Confirm compliance-sensitive workflows are included in validation and incident review

## API Endpoints

### Health

```http
GET /api/health
```

### Sample Architecture

```http
GET /api/simulations/sample/nodes
GET /api/simulations/sample/graph
```

### Sample Simulation Analysis

```http
POST /api/simulations/outage/analyze
POST /api/simulations/schema-change/analyze
```

### Custom Architecture Simulation Analysis

```http
POST /api/simulations/outage/custom/analyze
POST /api/simulations/schema-change/custom/analyze
POST /api/simulations/auth-failure/custom/analyze
```

## Custom Outage Request Example

```json
{
  "nodes": [
    {
      "id": "order-database",
      "label": "Order Database",
      "type": "database",
      "containsPii": false,
      "dataSensitivity": "confidential",
      "complianceTags": ["SOC2"]
    },
    {
      "id": "checkout-service",
      "label": "Checkout Service",
      "type": "service",
      "containsPii": false,
      "dataSensitivity": "confidential",
      "complianceTags": ["PCI", "SOC2"]
    },
    {
      "id": "web-app",
      "label": "Web App",
      "type": "frontend",
      "containsPii": false,
      "dataSensitivity": "internal",
      "complianceTags": []
    }
  ],
  "edges": [
    {
      "id": "edge-order-database-checkout-service",
      "sourceNode": "Order Database",
      "targetNode": "Checkout Service",
      "relationship": "supports"
    },
    {
      "id": "edge-checkout-service-web-app",
      "sourceNode": "Checkout Service",
      "targetNode": "Web App",
      "relationship": "supports"
    }
  ],
  "failedNode": "Order Database"
}
```

## Custom Auth Failure Request Example

```json
{
  "nodes": [
    {
      "id": "auth-service",
      "label": "Auth Service",
      "type": "service",
      "containsPii": false,
      "dataSensitivity": "confidential",
      "complianceTags": ["SOC2"]
    },
    {
      "id": "api-gateway",
      "label": "API Gateway",
      "type": "service",
      "containsPii": false,
      "dataSensitivity": "internal",
      "complianceTags": ["SOC2"]
    },
    {
      "id": "web-app",
      "label": "Web App",
      "type": "frontend",
      "containsPii": false,
      "dataSensitivity": "internal",
      "complianceTags": []
    }
  ],
  "edges": [
    {
      "id": "edge-auth-service-api-gateway",
      "sourceNode": "Auth Service",
      "targetNode": "API Gateway",
      "relationship": "supports"
    },
    {
      "id": "edge-api-gateway-web-app",
      "sourceNode": "API Gateway",
      "targetNode": "Web App",
      "relationship": "supports"
    }
  ],
  "authNode": "Auth Service"
}
```

## Example Response

```json
{
  "simulation": {
    "failedNode": "Auth Service",
    "severity": "high",
    "directlyAffected": ["API Gateway"],
    "indirectlyAffected": ["Web App"],
    "unaffected": [],
    "impactPaths": [
      ["Auth Service", "API Gateway"],
      ["Auth Service", "API Gateway", "Web App"]
    ],
    "explanation": "Auth Service failed as an authentication or authorization dependency. Direct consumers may lose login, token validation, session handling, or permission checks: API Gateway. Downstream systems that may also be affected through protected access paths: Web App. Severity is high."
  },
  "riskAssessment": {
    "riskScore": 88,
    "riskLevel": "high",
    "riskFactors": [
      "Authentication failure may affect login, token validation, session handling, or permission checks across dependent systems.",
      "The simulation affects 2 downstream system(s).",
      "Impact propagates beyond the first downstream dependency.",
      "The impact reaches a frontend or customer-facing surface.",
      "The simulation affects systems marked as confidential data environments.",
      "The simulation affects systems with compliance-related tags."
    ],
    "recommendations": [
      "Test login, logout, token refresh, expired-session handling, and protected-route access behavior.",
      "Confirm role-based permissions remain enforced during auth dependency failure.",
      "Validate indirect consumers, not just the first affected service.",
      "Run customer-facing smoke tests for login, logout, session expiry, and protected page access.",
      "Confirm compliance-sensitive workflows are included in post-change validation and incident review."
    ]
  }
}
```

## Running Locally

### Backend

From the backend directory:

```bash
cd backend
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

### Frontend

From the frontend directory:

```bash
cd frontend
npm install
npm run dev
```

The frontend runs on the Vite development URL shown in the terminal, usually:

```text
http://localhost:5173
```

## Deployment

SystemLens is deployed as two separate services:

- Frontend on Vercel
- Backend on Render

The frontend uses `VITE_API_BASE_URL` to call the deployed backend API.

The backend uses `CORS_ALLOWED_ORIGINS` to allow requests from the deployed frontend and local development environment.

## Project Structure

```text
reposcope/
  backend/
    Dockerfile
    src/main/java/com/reposcope/backend/
      config/
      controller/
      dto/
      engine/
      model/
      sample/
      service/
    src/main/resources/
      application.properties
  frontend/
    src/
      api/
      components/
      types.ts
      App.tsx
```

## Current Status

SystemLens currently supports:

- Custom user-defined architecture graphs
- Live graph visualization
- Outage simulation
- Schema-change simulation
- Auth-failure simulation
- Compliance and PII-aware node tagging
- Data-sensitivity metadata
- Blast-radius highlighting
- Risk scoring
- Risk factor generation
- Validation recommendations
- Separate frontend/backend deployment

## Future Improvements

Possible future improvements include:

- Export simulation reports
- Compare two simulation scenarios
- Add more starter architecture templates
- Add simulation history
- Add scenario diff view
- Expand backend test coverage for additional simulation and risk-scoring scenarios
- Add richer enterprise architecture presets

## What This Project Demonstrates

SystemLens demonstrates:

- Full-stack application development
- REST API design
- Graph traversal logic
- Type-safe frontend development with TypeScript
- Interactive architecture visualization
- Backend risk-analysis logic
- Compliance-aware systems modeling
- Auth and dependency failure modeling
- Docker-based backend deployment
- Environment-based frontend/backend configuration
- Product-oriented engineering and developer-tool design
