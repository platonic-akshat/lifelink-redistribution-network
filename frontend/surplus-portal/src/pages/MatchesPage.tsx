import { useCallback, useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import { approveMatch, listPendingMatches, rejectMatch } from "../api/matches";
import type { Match } from "../types";
import { ApiError } from "../api/client";
import EmptyState from "../components/EmptyState";
import LoadingBlock from "../components/LoadingBlock";
import PageHeader from "../components/PageHeader";

export default function MatchesPage() {
  const { coordinatorId } = useAuth();
  const [matches, setMatches] = useState<Match[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [rejectingId, setRejectingId] = useState<string | null>(null);
  const [rejectReason, setRejectReason] = useState("");
  const [pendingId, setPendingId] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await listPendingMatches();
      setMatches([...data].sort((a, b) => b.score - a.score));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to load matches.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  async function handleApprove(match: Match) {
    setPendingId(match.matchId);
    setError(null);
    try {
      await approveMatch(match.matchId, coordinatorId);
      await refresh();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Approve failed.");
    } finally {
      setPendingId(null);
    }
  }

  async function handleReject(match: Match) {
    const reason = rejectReason.trim();
    if (!reason) {
      setError("A reason is required to reject.");
      return;
    }
    setPendingId(match.matchId);
    setError(null);
    try {
      await rejectMatch(match.matchId, coordinatorId, reason);
      setRejectingId(null);
      setRejectReason("");
      await refresh();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Reject failed.");
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
        title="Match review"
        lead="Approve or reject proposed surplus–need pairings. Higher scores rank better fit."
        actions={
          <button type="button" className="secondary" disabled={loading} onClick={refresh}>
            {loading ? "Refreshing…" : "Refresh"}
          </button>
        }
      />

      {error && <p className="error">{error}</p>}

      {loading && matches.length === 0 ? (
        <LoadingBlock label="Loading pending matches…" />
      ) : !loading && matches.length === 0 ? (
        <EmptyState
          icon="🔗"
          title="No pending matches"
          description="Post surplus and needs in the same category and area — matching runs automatically via Kafka Streams."
        />
      ) : (
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>Need</th>
                <th>Surplus</th>
                <th>Score</th>
                <th>Distance</th>
                <th>Expires in</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {matches.map((match) => (
                <tr key={match.matchId}>
                  <td>{shortId(match.needId)}</td>
                  <td>{match.surplusIds.map(shortId).join(", ")}</td>
                  <td>
                    <span className="score-pill">{match.score.toFixed(2)}</span>
                  </td>
                  <td>{match.distanceKm.toFixed(1)} km</td>
                  <td>{match.hoursToExpiry.toFixed(1)} h</td>
                  <td className="actions-cell">
                    {rejectingId === match.matchId ? (
                      <div className="inline-form">
                        <input
                          value={rejectReason}
                          onChange={(e) => setRejectReason(e.target.value)}
                          placeholder="Rejection reason"
                        />
                        <button
                          type="button"
                          disabled={pendingId === match.matchId}
                          onClick={() => handleReject(match)}
                        >
                          Confirm
                        </button>
                        <button type="button" className="secondary" onClick={() => setRejectingId(null)}>
                          Cancel
                        </button>
                      </div>
                    ) : (
                      <>
                        <button
                          type="button"
                          disabled={pendingId === match.matchId}
                          onClick={() => handleApprove(match)}
                        >
                          Approve
                        </button>
                        <button
                          type="button"
                          className="secondary"
                          disabled={pendingId === match.matchId}
                          onClick={() => {
                            setRejectingId(match.matchId);
                            setRejectReason("");
                          }}
                        >
                          Reject
                        </button>
                      </>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
