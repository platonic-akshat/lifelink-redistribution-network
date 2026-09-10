import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { DecisionRequest, Match, RejectRequest } from '../models/match.model';

@Injectable({ providedIn: 'root' })
export class MatchesService {
  private readonly baseUrl = `${environment.apiBaseUrl}/api/matches`;

  constructor(private readonly http: HttpClient) {}

  getPending(): Observable<Match[]> {
    return this.http.get<Match[]>(this.baseUrl, { params: { status: 'PENDING' } });
  }

  approve(matchId: string, decidedBy: string): Observable<Match> {
    const body: DecisionRequest = { decidedBy };
    return this.http.post<Match>(`${this.baseUrl}/${matchId}/approve`, body);
  }

  reject(matchId: string, decidedBy: string, reason: string): Observable<Match> {
    const body: RejectRequest = { decidedBy, reason };
    return this.http.post<Match>(`${this.baseUrl}/${matchId}/reject`, body);
  }
}
