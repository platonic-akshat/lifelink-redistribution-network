import { useCallback, useEffect, useState } from "react";
import {
  listLogistics,
  markDelivered,
  markFailed,
  markPickedUp,
} from "../api/logistics";
import type { Logistics, LogisticsStatus } from "../types";
import EmptyState from "../components/EmptyState";
import LoadingBlock from "../components/LoadingBlock";
import PageHeader from "../components/PageHeader";
import { ApiError } from "../api/client";

const STATUS_CLASS: Record<LogisticsStatus, string> = {
  SCHEDULED: "badge badge-amber",
  PICKED_UP: "badge badge-blue",
  DELIVERED: "badge badge-green",
  FAILED: "badge badge-gray",
};

export default function LogisticsPage() {
  const [records, setRecords] = useState<Logistics[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [pendingId, setPendingId] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const page = await listLogistics();
      setRecords(page.content ?? []);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to load logistics.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  async function runAction(
    matchId: string,
    action: (id: string) => Promise<Logistics>,
  ) {
    setPendingId(matchId);
    setError(null);
    try {
      await action(matchId);
      await refresh();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Update failed.");
    } finally {
      setPendingId(null);
    }
  }

  function shortId(id: string): string {
    return id.length > 8 ? `${id.slice(0, 8)}…` : id;
  }

  return (
    <div className="page">
      <PageHeader
        title="Logistics"
        lead="Track pickups and deliveries for approved matches."
        actions={
          <button type="button" className="secondary" disabled={loading} onClick={refresh}>
            Refresh
          </button>
        }
      />

      {error && <p className="error">{error}</p>}
      {loading ? (
        <LoadingBlock label="Loading logistics…" />
      ) : records.length === 0 ? (
        <EmptyState
          icon="🚚"
          title="No active shipments"
          description="Approve a match first — a logistics record is created automatically."
        />
      ) : (
        <div className="card-grid">
          {records.map((row) => (
            <article key={row.matchId} className="card card-interactive">
              <div className="card-top">
                <span className="category-pill">Match {shortId(row.matchId)}</span>
              <span className={STATUS_CLASS[row.status]}>{row.status}</span>
            </div>
            <p className="muted">Updated {new Date(row.updatedAt).toLocaleString()}</p>
            <div className="action-row">
              {row.status === "SCHEDULED" && (
                <button
                  type="button"
                  disabled={pendingId === row.matchId}
                  onClick={() => runAction(row.matchId, markPickedUp)}
                >
                  Picked up
                </button>
              )}
              {row.status === "PICKED_UP" && (
                <>
                  <button
                    type="button"
                    disabled={pendingId === row.matchId}
                    onClick={() => runAction(row.matchId, markDelivered)}
                  >
                    Delivered
                  </button>
                  <button
                    type="button"
                    className="danger"
                    disabled={pendingId === row.matchId}
                    onClick={() => runAction(row.matchId, markFailed)}
                  >
                    Failed
                  </button>
                </>
              )}
            </div>
          </article>
        ))}
        </div>
      )}
    </div>
  );
}
