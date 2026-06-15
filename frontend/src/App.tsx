import { useState } from "react";
import "./App.css";

type HealthResponse = {
  status: string;
  service: string;
};

function App() {
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [error, setError] = useState<string>("");

  async function checkBackendHealth() {
    try {
      setError("");

      const response = await fetch("http://localhost:8080/api/health");

      if (!response.ok) {
        throw new Error("Backend returned an error");
      }

      const data: HealthResponse = await response.json();
      setHealth(data);
    } catch (err) {
      setError("Could not connect to the backend.");
      setHealth(null);
    }
  }

  return (
    <main className="app">
      <section className="hero">
        <p className="eyebrow">Codebase Intelligence Platform</p>

        <h1>RepoScope</h1>

        <p className="subtitle">
          Analyze public GitHub repositories and generate architecture maps,
          dependency graphs, and onboarding guides for developers entering
          unfamiliar codebases.
        </p>

        <div className="repo-form">
          <input
            type="text"
            placeholder="Paste a GitHub repo URL..."
            disabled
          />
          <button disabled>Analyze Repo</button>
        </div>

        <div className="health-card">
          <h2>Backend Connection</h2>
          <p>
            Test whether the React frontend can reach the Spring Boot API.
          </p>

          <button onClick={checkBackendHealth}>Check API Health</button>

          {health && (
            <div className="success">
              <p>Status: {health.status}</p>
              <p>Service: {health.service}</p>
            </div>
          )}

          {error && <p className="error">{error}</p>}
        </div>
      </section>
    </main>
  );
}

export default App;