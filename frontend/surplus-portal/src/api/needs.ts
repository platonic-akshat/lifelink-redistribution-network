import { apiRequest } from "./client";
import type { CreateNeedRequest, Need } from "../types";

export async function createNeed(request: CreateNeedRequest): Promise<Need> {
  return apiRequest<Need>("/api/needs", { method: "POST", body: request });
}

export async function listNeedsForRequester(
  requesterId: string,
): Promise<Need[]> {
  return apiRequest<Need[]>("/api/needs", { query: { requesterId } });
}

export async function cancelNeed(id: string): Promise<Need> {
  return apiRequest<Need>(`/api/needs/${id}/cancel`, { method: "POST" });
}
