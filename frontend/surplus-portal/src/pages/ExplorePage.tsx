import { useState, type FormEvent } from "react";
import { listNearbySurplus } from "../api/surplus";
import type { Surplus } from "../types";
import { ApiError } from "../api/client";
import PageHeader from "../components/PageHeader";

export default function ExplorePage() {
  const [lat, setLat] = useState("");
  const [lon, setLon] = useState("");
  const [radiusKm, setRadiusKm] = useState("10");
  const [results, setResults] = useState<Surplus[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function useMyLocation() {
    if (!navigator.geolocation) {
      setError("Geolocation not supported.");
      return;
    }
    setLoading(true);
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setLat(String(pos.coords.latitude));
        setLon(String(pos.coords.longitude));
        setLoading(false);
      },
      (e) => {
        setError(e.message);
        setLoading(false);
      },
    );
  }

  async function search(event: FormEvent) {
    event.preventDefault();
    const latitude = Number(lat);
    const longitude = Number(lon);
    const radius = Number(radiusKm);
    if (!Number.isFinite(latitude) || !Number.isFinite(longitude) || !Number.isFinite(radius)) {
      setError("Enter valid coordinates and radius.");
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const data = await listNearbySurplus(latitude, longitude, radius);
      setResults(data);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Search failed.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page">
      <PageHeader
        title="Explore nearby surplus"
        lead="Posted surplus only — exact addresses are not shown. Use matching workflows for fulfillment."
      />
      <form className="card page-narrow" onSubmit={search} style={{ marginBottom: "1.5rem" }}>
        <div className="field-row">
          <label className="field">
            <span>Latitude</span>
            <input value={lat} onChange={(e) => setLat(e.target.value)} />
          </label>
          <label className="field">
            <span>Longitude</span>
            <input value={lon} onChange={(e) => setLon(e.target.value)} />
          </label>
        </div>
        <label className="field">
          <span>Radius (km)</span>
          <input value={radiusKm} onChange={(e) => setRadiusKm(e.target.value)} />
        </label>
        <button type="button" className="secondary" onClick={useMyLocation}>
          Use my location
        </button>
        {error && <p className="error">{error}</p>}
        <button type="submit" disabled={loading}>
          {loading ? "Searching…" : "Search"}
        </button>
      </form>

      <div className="card-grid">
        {results.map((s) => (
          <article key={s.id} className="card card-interactive">
            <div className="card-top">
              <span className="category-pill">{s.category}</span>
              <span className="badge badge-green">{s.status}</span>
            </div>
            <h3>{s.subType}</h3>
            <p className="muted">
              {s.quantity} {s.unit} · {s.providerName}
            </p>
          </article>
        ))}
      </div>
    </div>
  );
}
