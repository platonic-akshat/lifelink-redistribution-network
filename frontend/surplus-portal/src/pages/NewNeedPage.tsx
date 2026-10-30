import { useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { createNeed } from "../api/needs";
import type { SurplusCategory, Urgency } from "../types";
import { ApiError } from "../api/client";

const CATEGORIES: SurplusCategory[] = ["MEDICINE", "BLOOD", "FOOD"];
const URGENCIES: Urgency[] = ["CRITICAL", "HIGH", "NORMAL"];

export default function NewNeedPage() {
  const { requesterId, requesterName } = useAuth();
  const navigate = useNavigate();

  const [category, setCategory] = useState<SurplusCategory>("FOOD");
  const [subType, setSubType] = useState("");
  const [quantity, setQuantity] = useState("");
  const [unit, setUnit] = useState("");
  const [urgency, setUrgency] = useState<Urgency>("NORMAL");
  const [latitude, setLatitude] = useState("");
  const [longitude, setLongitude] = useState("");
  const [locating, setLocating] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  function handleUseMyLocation() {
    if (!navigator.geolocation) {
      setError("Geolocation is not supported in this browser.");
      return;
    }
    setLocating(true);
    setError(null);
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setLatitude(String(position.coords.latitude));
        setLongitude(String(position.coords.longitude));
        setLocating(false);
      },
      (geoError) => {
        setError(`Could not get location: ${geoError.message}`);
        setLocating(false);
      },
    );
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    if (!requesterName) {
      setError("Set your organization name on the Needs page first.");
      return;
    }

    const qty = Number(quantity);
    const lat = Number(latitude);
    const lon = Number(longitude);
    if (!subType.trim() || !unit.trim() || !Number.isFinite(qty) || qty <= 0) {
      setError("Check subtype, unit, and quantity.");
      return;
    }
    if (!Number.isFinite(lat) || !Number.isFinite(lon)) {
      setError("Enter a valid location or use “Use my location”.");
      return;
    }

    setSubmitting(true);
    try {
      await createNeed({
        category,
        subType: subType.trim(),
        quantity: qty,
        unit: unit.trim(),
        urgency,
        latitude: lat,
        longitude: lon,
        requesterId,
        requesterName,
      });
      navigate("/needs");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to create need.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page page-narrow">
      <h1>Create need</h1>
      <form className="card" onSubmit={handleSubmit}>
        <label className="field">
          <span>Category</span>
          <select value={category} onChange={(e) => setCategory(e.target.value as SurplusCategory)}>
            {CATEGORIES.map((c) => (
              <option key={c} value={c}>
                {c}
              </option>
            ))}
          </select>
        </label>
        <label className="field">
          <span>Subtype / description</span>
          <input value={subType} onChange={(e) => setSubType(e.target.value)} />
        </label>
        <div className="field-row">
          <label className="field">
            <span>Quantity</span>
            <input value={quantity} onChange={(e) => setQuantity(e.target.value)} inputMode="decimal" />
          </label>
          <label className="field">
            <span>Unit</span>
            <input value={unit} onChange={(e) => setUnit(e.target.value)} placeholder="portions, units…" />
          </label>
        </div>
        <label className="field">
          <span>Urgency</span>
          <select value={urgency} onChange={(e) => setUrgency(e.target.value as Urgency)}>
            {URGENCIES.map((u) => (
              <option key={u} value={u}>
                {u}
              </option>
            ))}
          </select>
        </label>
        <div className="field-row">
          <label className="field">
            <span>Latitude</span>
            <input value={latitude} onChange={(e) => setLatitude(e.target.value)} />
          </label>
          <label className="field">
            <span>Longitude</span>
            <input value={longitude} onChange={(e) => setLongitude(e.target.value)} />
          </label>
        </div>
        <button type="button" className="secondary" disabled={locating} onClick={handleUseMyLocation}>
          {locating ? "Locating…" : "Use my location"}
        </button>
        {error && <p className="error">{error}</p>}
        <button type="submit" disabled={submitting}>
          {submitting ? "Posting…" : "Post need"}
        </button>
      </form>
    </div>
  );
}
