import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

/**
 * Attaches `Authorization: Bearer <token>` to every outgoing request when a
 * token is present. Functional interceptor (Angular 17/18 style), wired up
 * via provideHttpClient(withInterceptors([...])) in app.config.ts.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthService).getToken();

  if (!token) {
    return next(req);
  }

  const authReq = req.clone({
    setHeaders: { Authorization: `Bearer ${token}` }
  });

  return next(authReq);
};
