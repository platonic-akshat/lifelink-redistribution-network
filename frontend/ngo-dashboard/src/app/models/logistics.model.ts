export type LogisticsStatus = 'SCHEDULED' | 'PICKED_UP' | 'DELIVERED' | 'FAILED';

export interface Logistics {
  matchId: string;
  status: LogisticsStatus;
  notes: string | null;
  updatedAt: string; // ISO instant
}

export interface LogisticsActionRequest {
  notes?: string;
}
