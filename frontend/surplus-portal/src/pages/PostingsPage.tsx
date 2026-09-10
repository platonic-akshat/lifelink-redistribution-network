import { useCallback, useEffect, useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { listSurplusForProvider, withdrawSurplus } from "../api/surplus";
import type { Surplus, SurplusStatus } from "../types";
import { formatRelativeExpiry } from "../utils/time";
import { ApiError } from "../api/client";

const STATUS_CLASS: Record<SurplusStatus, string> = {
  POSTED: "badge badge-green",
  EXPIRING_SOON: "badge badge-amber",
  CLAIMED: "badge badge-blue",
  EXPIRED: "badge badge-gray",
  COMPLETED: "badge badge-gray",
};

export default function PostingsPage() {
  const { providerId, providerName, setProviderName } = useAuth();

  const [postings, setPostings] = useState<Surplus[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [withdrawingId, setWithdrawingId] = useState<string | null>(null);
  const [nameDraft, setNameDraft] = useState(providerName);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await listSurplusForProvider(providerId);
      setPostings(data);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to load postings.");
    } finally {
      setLoading(false);
    }
  }, [providerId]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  async function handleWithdraw(id: string) {
    setWithdrawingId(id);
    setError(null);
    try {
      await withdrawSurplus(id);
      await refresh();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to withdraw posting.");
    } finally {
      setWithdrawingId(null);
    }
  }

  function handleSaveName(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (nameDraft.trim()) {
      setProviderName(nameDraft.trim());
    }
  }

  if (!providerName) {
    return (
      <div className="page page-narrow">
        <h1>Welcome to LifeLink</h1>
        <p className="muted">
          Before you post surplus, tell us who's posting. This is stored on
          this device and sent with every posting you create.
        </p>
        <form className="card" onSubmit={handleSaveName}>
          <label className="field">
            <span>Organization / provider name</span>
            <input
              type="text"
              value={nameDraft}
              onChange={(e) => setNameDraft(e.target.value)}
              placeholder="e.g. St. Mary's Hospital"
              autoFocus
            />
          </label>
          <button type="submit">Continue</button>
        </form>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>Your Postings</h1>
          <p className="muted">Posting as {providerName}</p>
        </div>
        <Link className="button-link" to="/postings/new">
          + New Posting
        </Link>
      </div>

      {error && <p className="error">{error}</p>}

      {loading ? (
        <p className="muted">Loading...</p>
      ) : postings.length === 0 ? (
        <p className="muted">No postings yet. Create your first one.</p>
      ) : (
        <div className="card-grid">
          {postings.map((posting) => (
            <div className="card" key={posting.id}>
              <div className="card-top">
                <span className="category">{posting.category}</span>
                <span className={STATUS_CLASS[posting.status]}>
                  {posting.status.replace("_", " ")}
                </span>
              </div>
              <h3>{posting.subType}</h3>
              <p>
                {posting.quantity} {posting.unit}
              </p>
              <p className="muted">{formatRelativeExpiry(posting.expiresAt)}</p>
              <button
                className="danger"
                onClick={() => handleWithdraw(posting.id)}
                disabled={withdrawingId === posting.id}
              >
                {withdrawingId === posting.id ? "Withdrawing..." : "Withdraw"}
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
