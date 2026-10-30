// Shared types mirroring backend API contracts (see ARCHITECTURE.md).

export type SurplusCategory = "MEDICINE" | "BLOOD" | "FOOD";

export type SurplusStatus =
  | "POSTED"
  | "CLAIMED"
  | "EXPIRING_SOON"
  | "EXPIRED"
  | "COMPLETED";

export type NeedStatus = "POSTED" | "MATCHED" | "FULFILLED" | "CANCELLED";

export type Urgency = "CRITICAL" | "HIGH" | "NORMAL";

export type UserRole = "PROVIDER" | "NGO" | "COORDINATOR";

export type MatchStatus = "PENDING" | "APPROVED" | "REJECTED";

export type LogisticsStatus = "SCHEDULED" | "PICKED_UP" | "DELIVERED" | "FAILED";

export interface CreateSurplusRequest {
  category: SurplusCategory;
  subType: string;
  quantity: number;
  unit: string;
  latitude: number;
  longitude: number;
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

export interface CreateNeedRequest {
  category: SurplusCategory;
  subType: string;
  quantity: number;
  unit: string;
  urgency: Urgency;
  latitude: number;
  longitude: number;
  requesterId: string;
  requesterName: string;
}

export interface Need extends CreateNeedRequest {
  id: string;
  status: NeedStatus;
  createdAt: string;
  updatedAt: string;
}

export interface Match {
  matchId: string;
  needId: string;
  surplusIds: string[];
  score: number;
  distanceKm: number;
  hoursToExpiry: number;
  status: MatchStatus;
  proposedAt: string;
}

export interface Logistics {
  matchId: string;
  status: LogisticsStatus;
  notes: string | null;
  updatedAt: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface LoginRequest {
  username: string;
  password: string;
  role: UserRole;
}

export interface LoginResponse {
  token: string;
}
