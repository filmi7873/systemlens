import CustomArchitectureBuilder from "./components/CustomArchitectureBuilder";
import "./App.css";

function App() {
  return (
    <main className="app">
      <section className="hero">
        <p className="eyebrow">Architecture Simulation Platform</p>

        <h1>SystemLens</h1>

        <p className="subtitle">
          Model software systems as dependency graphs, simulate outages and
          schema changes, and understand downstream blast radius before
          production.
        </p>

        <CustomArchitectureBuilder />
      </section>
    </main>
  );
}

export default App;