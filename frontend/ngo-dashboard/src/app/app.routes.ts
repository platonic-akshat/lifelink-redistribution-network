import { Routes } from '@angular/router';
import { authGuard } from './auth/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'matches' },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then((m) => m.LoginComponent)
  },
  {
    path: 'matches',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/matches/matches.component').then((m) => m.MatchesComponent)
  },
  {
    path: 'logistics',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/logistics/logistics.component').then((m) => m.LogisticsComponent)
  },
  { path: '**', redirectTo: 'matches' }
];
