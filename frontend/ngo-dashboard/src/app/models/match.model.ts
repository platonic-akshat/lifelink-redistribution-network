export type MatchStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface Match {
  matchId: string;
  needId: string;
  surplusIds: string[];
  score: number;
  distanceKm: number;
  hoursToExpiry: number;
  status: MatchStatus;
  proposedAt: string; // ISO instant
}

export interface DecisionRequest {
  decidedBy: string;
}

export interface RejectRequest extends DecisionRequest {
  reason: string;
}
