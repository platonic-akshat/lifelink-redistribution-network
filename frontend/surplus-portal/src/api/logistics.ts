import { apiRequest } from "./client";
import type { Logistics, LogisticsStatus, Page } from "../types";

export async function listLogistics(
  status?: LogisticsStatus,
  page = 0,
  size = 50,
): Promise<Page<Logistics>> {
  return apiRequest<Page<Logistics>>("/api/logistics", {
    query: {
      page,
      size,
      sort: "updatedAt,desc",
      status,
    },
  });
}

export async function markPickedUp(matchId: string, notes?: string): Promise<Logistics> {
  return apiRequest<Logistics>(`/api/logistics/${matchId}/picked-up`, {
    method: "POST",
    body: notes ? { notes } : {},
  });
}

export async function markDelivered(matchId: string, notes?: string): Promise<Logistics> {
  return apiRequest<Logistics>(`/api/logistics/${matchId}/delivered`, {
    method: "POST",
    body: notes ? { notes } : {},
  });
}

export async function markFailed(matchId: string, notes?: string): Promise<Logistics> {
  return apiRequest<Logistics>(`/api/logistics/${matchId}/failed`, {
    method: "POST",
    body: notes ? { notes } : {},
  });
}
