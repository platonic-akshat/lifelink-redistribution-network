import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginRequest, LoginResponse } from '../models/auth.model';
import { STORAGE_KEYS } from '../core/storage-keys';

/**
 * DEMO / STARTER AUTH FLOW:
 * There is no real signup or credential validation on the client. Any
 * non-blank username/password is sent to the backend's login stub, which
 * (per the starter api-gateway) issues a JWT for whatever it's given.
 * This is intentionally minimal — replace with a real identity provider
 * (Keycloak/Auth0/etc.) before this ever sees production traffic.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private readonly http: HttpClient) {
    this.ensureCoordinatorId();
  }

  login(username: string, password: string): Observable<LoginResponse> {
    const body: LoginRequest = { username, password, role: 'NGO' };
    return this.http
      .post<LoginResponse>(`${environment.apiBaseUrl}/api/auth/login`, body)
      .pipe(tap((res) => this.setToken(res.token)));
  }

  logout(): void {
    localStorage.removeItem(STORAGE_KEYS.authToken);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getToken(): string | null {
    return localStorage.getItem(STORAGE_KEYS.authToken);
  }

  private setToken(token: string): void {
    localStorage.setItem(STORAGE_KEYS.authToken, token);
  }

  /**
   * The demo coordinator identity used as `decidedBy` on every approve/reject
   * call. Generated once per browser and persisted, since there's no real
   * per-user account system in this starter.
   */
  getCoordinatorId(): string {
    return this.ensureCoordinatorId();
  }

  private ensureCoordinatorId(): string {
    let id = localStorage.getItem(STORAGE_KEYS.coordinatorId);
    if (!id) {
      id = crypto.randomUUID();
      localStorage.setItem(STORAGE_KEYS.coordinatorId, id);
    }
    return id;
  }
}
