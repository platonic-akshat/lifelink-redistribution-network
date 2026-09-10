// Shared types mirroring the surplus-service API contract (see ARCHITECTURE.md).

export type SurplusCategory = "MEDICINE" | "BLOOD" | "FOOD";

export type SurplusStatus =
  | "POSTED"
  | "CLAIMED"
  | "EXPIRING_SOON"
  | "EXPIRED"
  | "COMPLETED";

export interface CreateSurplusRequest {
  category: SurplusCategory;
  subType: string;
  quantity: number;
  unit: string;
  latitude: number;
  longitude: number;
  /** ISO-8601 instant, must be in the future. */
  expiresAt: string;
  providerId: string;
  providerName: string;
}

export interface Surplus extends CreateSurplusRequest {
  id: string;
  status: SurplusStatus;
  createdAt: string;
  updatedAt: string;
}

export interface LoginRequest {
  username: string;
  password: string;
  role: "PROVIDER";
}

export interface LoginResponse {
  token: string;
}
