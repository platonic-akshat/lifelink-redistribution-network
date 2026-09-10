import { apiRequest } from "./client";
import type { CreateSurplusRequest, Surplus } from "../types";

export async function createSurplus(
  request: CreateSurplusRequest,
): Promise<Surplus> {
  return apiRequest<Surplus>("/api/surplus", {
    method: "POST",
    body: request,
  });
}

export async function listSurplusForProvider(
  providerId: string,
): Promise<Surplus[]> {
  return apiRequest<Surplus[]>("/api/surplus", {
    query: { providerId },
  });
}

export async function withdrawSurplus(id: string): Promise<void> {
  await apiRequest<void>(`/api/surplus/${id}`, { method: "DELETE" });
}

// Not used by this app's UI today, but exposed for completeness / future use
// (e.g. a "what's near me" view).
export async function listNearbySurplus(
  lat: number,
  lon: number,
  radiusKm: number,
): Promise<Surplus[]> {
  return apiRequest<Surplus[]>("/api/surplus/nearby", {
    query: { lat, lon, radiusKm },
  });
}
