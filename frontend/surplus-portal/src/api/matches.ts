import { apiRequest } from "./client";
import type { Match } from "../types";

export async function listPendingMatches(): Promise<Match[]> {
  return apiRequest<Match[]>("/api/matches", { query: { status: "PENDING" } });
}

export async function approveMatch(
  matchId: string,
  decidedBy: string,
): Promise<Match> {
  return apiRequest<Match>(`/api/matches/${matchId}/approve`, {
    method: "POST",
    body: { decidedBy },
  });
}

export async function rejectMatch(
  matchId: string,
  decidedBy: string,
  reason: string,
): Promise<Match> {
  return apiRequest<Match>(`/api/matches/${matchId}/reject`, {
    method: "POST",
    body: { decidedBy, reason },
  });
}
