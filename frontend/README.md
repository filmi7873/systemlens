# System Lens

System Lens is a full-stack architecture simulation platform for modeling software systems as dependency graphs and analyzing downstream impact from outages and schema changes.

The app uses a sample e-commerce architecture made up of frontend services, backend services, databases, queues, external providers, and workers. Users can select a system node, run an outage or schema-change simulation, and view direct impact, indirect impact, unaffected systems, impact paths, and a deployment readiness risk assessment.

## Features

- Interactive system dependency graph
- Outage impact simulation
- Schema-change impact simulation
- Direct vs. indirect downstream impact classification
- Impact path generation through graph traversal
- Risk assessment and deployment readiness report
- React frontend connected to a Spring Boot API
- Backend health check endpoint

## Tech Stack

### Frontend
- React
- TypeScript
- Vite
- React Flow
- CSS

### Backend
- Java
- Spring Boot
- Maven
- REST APIs

## How It Works

System Lens represents architecture dependencies as directed edges.

For example:

```text
Payment Provider -> Checkout Service -> Web App
```
## API Endpoints

#### Health Check
- GET /api/health
#### Get Sample Nodes
- GET /api/simulations/sample/nodes
#### Get Sample Graph
- GET /api/simulations/sample/graph
#### Run Outage Simulation
- POST /api/simulations/outage/analyze

#### Request body:
```bash 
{
  "failedNode": "Payment Provider"
}
```

#### Run Schema Change Simulation
```bash 
POST /api/simulations/schema-change/analyze
```

#### Request body:
```bash 
{
  "changedNode": "Inventory Database"
}
``` 
#### Running Locally
**Backend**

From the backend folder:

./mvnw spring-boot:run

On Windows PowerShell:

.\mvnw.cmd spring-boot:run

The backend runs on:

http://localhost:8080
**Frontend**

From the frontend folder:

npm install
npm run dev

The frontend runs on:

http://localhost:5173
## Project Status

System Lens currently uses a predefined sample architecture to demonstrate the core simulation engine. Future improvements could include creating custom systems from the UI, saving architectures to a database, importing service maps, and adding authentication for multiple users.


## My next recommendation

Do these in order:

1. Add the missing `Notification Worker -> Web App` edge.
2. Add cycle protection to `OutageSimulationEngine`.
3. Check `SchemaChangeSimulationEngine` to make sure it is also dynamic.
4. Replace the README.
5. Make the default selected node `Payment Provider` instead of `Web App`.

Then this project is honestly portfolio-presentable.