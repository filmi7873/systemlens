import { useState } from "react";
import "./App.css";

type HealthResponse = {
  status: string;
  service: string;
};

type AnalyzeRepositoryResponse = {
  owner: string;
  repo: string;
  url: string;
  status: string;
};

function App() {
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [repoUrl, setRepoUrl] = useState("");
  const [analysis, setAnalysis] = useState<AnalyzeRepositoryResponse | null>(
    null
  );
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

  async function analyzeRepository(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    try {
      setError("");
      setAnalysis(null);

      const response = await fetch(
        "http://localhost:8080/api/repositories/analyze",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ url: repoUrl }),
        }
      );

      if (!response.ok) {
        throw new Error("Repository analysis failed.");
      }

      const data: AnalyzeRepositoryResponse = await response.json();
      setAnalysis(data);
    } catch (err) {
      setError("Could not analyze repository. Check the URL and try again.");
      setAnalysis(null);
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

        <form className="repo-form" onSubmit={analyzeRepository}>
          <input
            type="text"
            placeholder="Paste a GitHub repo URL..."
            value={repoUrl}
            onChange={(event) => setRepoUrl(event.target.value)}
          />
          <button type="submit">Analyze Repo</button>
        </form>

        {analysis && (
          <div className="success">
            <p>Owner: {analysis.owner}</p>
            <p>Repository: {analysis.repo}</p>
            <p>Status: {analysis.status}</p>
          </div>
        )}

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