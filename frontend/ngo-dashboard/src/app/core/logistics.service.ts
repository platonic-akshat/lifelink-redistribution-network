import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Logistics, LogisticsActionRequest } from '../models/logistics.model';
import { STORAGE_KEYS } from './storage-keys';

@Injectable({ providedIn: 'root' })
export class LogisticsService {
  private readonly baseUrl = `${environment.apiBaseUrl}/api/logistics`;

  constructor(private readonly http: HttpClient) {}

  get(matchId: string): Observable<Logistics> {
    return this.http.get<Logistics>(`${this.baseUrl}/${matchId}`);
  }

  markPickedUp(matchId: string, notes?: string): Observable<Logistics> {
    return this.http.post<Logistics>(`${this.baseUrl}/${matchId}/picked-up`, this.body(notes));
  }

  markDelivered(matchId: string, notes?: string): Observable<Logistics> {
    return this.http.post<Logistics>(`${this.baseUrl}/${matchId}/delivered`, this.body(notes));
  }

  markFailed(matchId: string, notes?: string): Observable<Logistics> {
    return this.http.post<Logistics>(`${this.baseUrl}/${matchId}/failed`, this.body(notes));
  }

  private body(notes?: string): LogisticsActionRequest {
    return notes ? { notes } : {};
  }

  /**
   * Tracks matchIds approved this "session" (persisted in localStorage so a
   * refresh doesn't lose the list) so the /logistics page knows which
   * matches to poll for logistics status.
   */
  addApprovedMatchId(matchId: string): void {
    const ids = this.getApprovedMatchIds();
    if (!ids.includes(matchId)) {
      ids.push(matchId);
      localStorage.setItem(STORAGE_KEYS.approvedMatchIds, JSON.stringify(ids));
    }
  }

  getApprovedMatchIds(): string[] {
    const raw = localStorage.getItem(STORAGE_KEYS.approvedMatchIds);
    if (!raw) {
      return [];
    }
    try {
      const parsed = JSON.parse(raw);
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  }

  list(status?: string, page = 0, size = 50): Observable<{ content: Logistics[]; totalElements: number }> {
    const params: Record<string, string> = {
      page: String(page),
      size: String(size),
      sort: 'updatedAt,desc',
    };
    if (status) {
      params['status'] = status;
    }
    return this.http.get<{ content: Logistics[]; totalElements: number }>(this.baseUrl, { params });
  }
}
