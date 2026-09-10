import { useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { createSurplus } from "../api/surplus";
import type { SurplusCategory } from "../types";
import { ApiError } from "../api/client";

const CATEGORIES: SurplusCategory[] = ["MEDICINE", "BLOOD", "FOOD"];

// datetime-local inputs give "YYYY-MM-DDTHH:mm" in local time with no
// timezone info; `new Date(...)` parses that as local time, and
// `.toISOString()` converts it to a UTC ISO-8601 instant for the API.
function datetimeLocalToIso(value: string): string {
  return new Date(value).toISOString();
}

export default function NewPostingPage() {
  const { providerId, providerName } = useAuth();
  const navigate = useNavigate();

  const [category, setCategory] = useState<SurplusCategory>("MEDICINE");
  const [subType, setSubType] = useState("");
  const [quantity, setQuantity] = useState("");
  const [unit, setUnit] = useState("");
  const [latitude, setLatitude] = useState("");
  const [longitude, setLongitude] = useState("");
  const [expiresAt, setExpiresAt] = useState("");
  const [locating, setLocating] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  function handleUseMyLocation() {
    if (!navigator.geolocation) {
      setError("Geolocation is not supported by this browser.");
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

    const quantityNum = Number(quantity);
    const latNum = Number(latitude);
    const lonNum = Number(longitude);

    if (!subType.trim() || !unit.trim() || !expiresAt) {
      setError("Please fill in all required fields.");
      return;
    }
    if (!Number.isFinite(quantityNum) || quantityNum <= 0) {
      setError("Quantity must be a positive number.");
      return;
    }
    if (!Number.isFinite(latNum) || !Number.isFinite(lonNum)) {
      setError("Latitude and longitude must be numbers.");
      return;
    }

    const isoExpiresAt = datetimeLocalToIso(expiresAt);
    if (new Date(isoExpiresAt).getTime() <= Date.now()) {
      setError("Expiry must be in the future.");
      return;
    }

    setSubmitting(true);
    try {
      await createSurplus({
        category,
        subType: subType.trim(),
        quantity: quantityNum,
        unit: unit.trim(),
        latitude: latNum,
        longitude: lonNum,
        expiresAt: isoExpiresAt,
        providerId,
        providerName,
      });
      navigate("/postings", { replace: true });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to create posting.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page page-narrow">
      <h1>New Surplus Posting</h1>
      <form className="card" onSubmit={handleSubmit}>
        <label className="field">
          <span>Category</span>
          <select
            value={category}
            onChange={(e) => setCategory(e.target.value as SurplusCategory)}
          >
            {CATEGORIES.map((c) => (
              <option key={c} value={c}>
                {c}
              </option>
            ))}
          </select>
        </label>

        <label className="field">
          <span>Sub-type</span>
          <input
            type="text"
            value={subType}
            onChange={(e) => setSubType(e.target.value)}
            placeholder="e.g. Insulin, O+ whole blood, Cooked rice"
          />
        </label>

        <div className="field-row">
          <label className="field">
            <span>Quantity</span>
            <input
              type="number"
              min="0"
              step="any"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
            />
          </label>
          <label className="field">
            <span>Unit</span>
            <input
              type="text"
              value={unit}
              onChange={(e) => setUnit(e.target.value)}
              placeholder="e.g. vials, units, kg"
            />
          </label>
        </div>

        <div className="field-row">
          <label className="field">
            <span>Latitude</span>
            <input
              type="number"
              step="any"
              value={latitude}
              onChange={(e) => setLatitude(e.target.value)}
            />
          </label>
          <label className="field">
            <span>Longitude</span>
            <input
              type="number"
              step="any"
              value={longitude}
              onChange={(e) => setLongitude(e.target.value)}
            />
          </label>
        </div>
        <button
          type="button"
          className="secondary"
          onClick={handleUseMyLocation}
          disabled={locating}
        >
          {locating ? "Locating..." : "Use my location"}
        </button>

        <label className="field">
          <span>Expires at</span>
          <input
            type="datetime-local"
            value={expiresAt}
            onChange={(e) => setExpiresAt(e.target.value)}
          />
        </label>

        {error && <p className="error">{error}</p>}

        <button type="submit" disabled={submitting}>
          {submitting ? "Posting..." : "Post Surplus"}
        </button>
      </form>
    </div>
  );
}
