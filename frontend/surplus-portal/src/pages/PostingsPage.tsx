import { useCallback, useEffect, useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { listSurplusForProvider, withdrawSurplus } from "../api/surplus";
import type { Surplus, SurplusCategory, SurplusStatus } from "../types";
import { formatRelativeExpiry } from "../utils/time";
import { ApiError } from "../api/client";
import EmptyState from "../components/EmptyState";
import LoadingBlock from "../components/LoadingBlock";
import PageHeader from "../components/PageHeader";

const STATUS_CLASS: Record<SurplusStatus, string> = {
  POSTED: "badge badge-green",
  EXPIRING_SOON: "badge badge-amber",
  CLAIMED: "badge badge-blue",
  EXPIRED: "badge badge-gray",
  COMPLETED: "badge badge-gray",
};

const CATEGORY_ICON: Record<SurplusCategory, string> = {
  MEDICINE: "💊",
  BLOOD: "🩸",
  FOOD: "🍽",
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
        <PageHeader
          title="Welcome to LifeLink"
          lead="Tell us your organization name once — we store it on this device and send it with every surplus you post."
        />
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
      <PageHeader
        title="Your surplus"
        lead={`Posting as ${providerName}. Withdraw listings you no longer want matched.`}
        actions={
          <Link className="button-link" to="/postings/new">
            + New posting
          </Link>
        }
      />

      {error && <p className="error">{error}</p>}

      {loading ? (
        <LoadingBlock label="Loading your postings…" />
      ) : postings.length === 0 ? (
        <EmptyState
          icon="📦"
          title="No surplus posted yet"
          description="Create a listing so matching can pair it with nearby needs before expiry."
          actionLabel="Create your first posting"
          actionTo="/postings/new"
        />
      ) : (
        <div className="card-grid">
          {postings.map((posting) => (
            <div className="card card-interactive" key={posting.id}>
              <div className="card-top">
                <span className="category-pill">
                  {CATEGORY_ICON[posting.category]} {posting.category}
                </span>
                <span className={STATUS_CLASS[posting.status]}>
                  {posting.status.replace("_", " ")}
                </span>
              </div>
              <h3>{posting.subType}</h3>
              <p>
                <strong>{posting.quantity}</strong> {posting.unit}
              </p>
              <p className="muted">{formatRelativeExpiry(posting.expiresAt)}</p>
              <button
                className="danger"
                onClick={() => handleWithdraw(posting.id)}
                disabled={withdrawingId === posting.id}
              >
                {withdrawingId === posting.id ? "Withdrawing…" : "Withdraw"}
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
