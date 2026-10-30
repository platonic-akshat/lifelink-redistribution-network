import { useCallback, useEffect, useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { cancelNeed, listNeedsForRequester } from "../api/needs";
import type { Need, NeedStatus } from "../types";
import { ApiError } from "../api/client";
import EmptyState from "../components/EmptyState";
import LoadingBlock from "../components/LoadingBlock";
import PageHeader from "../components/PageHeader";

const STATUS_CLASS: Record<NeedStatus, string> = {
  POSTED: "badge badge-green",
  MATCHED: "badge badge-blue",
  FULFILLED: "badge badge-gray",
  CANCELLED: "badge badge-gray",
};

export default function NeedsPage() {
  const { requesterId, requesterName, setRequesterName } = useAuth();
  const [needs, setNeeds] = useState<Need[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [nameDraft, setNameDraft] = useState(requesterName);
  const [cancellingId, setCancellingId] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await listNeedsForRequester(requesterId);
      setNeeds(data);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to load needs.");
    } finally {
      setLoading(false);
    }
  }, [requesterId]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  function handleSaveName(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (nameDraft.trim()) {
      setRequesterName(nameDraft.trim());
    }
  }

  async function handleCancel(id: string) {
    setCancellingId(id);
    setError(null);
    try {
      await cancelNeed(id);
      await refresh();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to cancel need.");
    } finally {
      setCancellingId(null);
    }
  }

  if (!requesterName) {
    return (
      <div className="page page-narrow">
        <PageHeader
          title="Welcome to LifeLink"
          lead="Set your organization name before posting needs."
        />
        <form className="card" onSubmit={handleSaveName}>
          <label className="field">
            <span>Organization name</span>
            <input
              value={nameDraft}
              onChange={(e) => setNameDraft(e.target.value)}
              placeholder="e.g. City Relief NGO"
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
        title="Needs"
        lead={`Requests from ${requesterName}. Cancel open needs you no longer require.`}
        actions={
          <Link className="button-link" to="/needs/new">
            Create need
          </Link>
        }
      />

      {error && <p className="error">{error}</p>}
      {loading ? (
        <LoadingBlock />
      ) : needs.length === 0 ? (
        <EmptyState
          icon="🆘"
          title="No needs posted"
          description="Create a need in your area so matching can find surplus donors nearby."
          actionLabel="Create need"
          actionTo="/needs/new"
        />
      ) : (
        <div className="card-grid">
          {needs.map((need) => (
            <article key={need.id} className="card card-interactive">
              <div className="card-top">
                <span className="category-pill">{need.category}</span>
              <span className={STATUS_CLASS[need.status]}>{need.status}</span>
            </div>
            <h3>{need.subType}</h3>
            <p className="muted">
              {need.quantity} {need.unit} · {need.urgency} urgency
            </p>
            {need.status === "POSTED" && (
              <button
                type="button"
                className="danger"
                disabled={cancellingId === need.id}
                onClick={() => handleCancel(need.id)}
              >
                {cancellingId === need.id ? "Cancelling…" : "Cancel need"}
              </button>
            )}
          </article>
        ))}
        </div>
      )}
    </div>
  );
}
