import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatchesService } from '../../core/matches.service';
import { LogisticsService } from '../../core/logistics.service';
import { AuthService } from '../../auth/auth.service';
import { Match } from '../../models/match.model';

@Component({
  selector: 'app-matches',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './matches.component.html',
  styleUrl: './matches.component.css'
})
export class MatchesComponent implements OnInit {
  matches: Match[] = [];
  isLoading = false;
  errorMessage: string | null = null;

  /** matchId currently showing an inline reject-reason input */
  rejectingMatchId: string | null = null;
  rejectReason = '';

  /** matchId currently mid-flight on approve/reject, to disable its buttons */
  pendingActionMatchId: string | null = null;

  constructor(
    private readonly matchesService: MatchesService,
    private readonly logisticsService: LogisticsService,
    private readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    this.fetchPending();
  }

  fetchPending(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.matchesService.getPending().subscribe({
      next: (matches) => {
        this.matches = [...matches].sort((a, b) => b.score - a.score);
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load pending matches.';
        this.isLoading = false;
      }
    });
  }

  approve(match: Match): void {
    this.pendingActionMatchId = match.matchId;
    // Optimistic removal from the pending list.
    this.matches = this.matches.filter((m) => m.matchId !== match.matchId);

    const decidedBy = this.authService.getCoordinatorId();
    this.matchesService.approve(match.matchId, decidedBy).subscribe({
      next: () => {
        this.logisticsService.addApprovedMatchId(match.matchId);
        this.pendingActionMatchId = null;
        this.fetchPending();
      },
      error: () => {
        this.errorMessage = `Failed to approve match ${this.shortId(match.matchId)}.`;
        this.pendingActionMatchId = null;
        this.fetchPending();
      }
    });
  }

  startReject(match: Match): void {
    this.rejectingMatchId = match.matchId;
    this.rejectReason = '';
  }

  cancelReject(): void {
    this.rejectingMatchId = null;
    this.rejectReason = '';
  }

  confirmReject(match: Match): void {
    const reason = this.rejectReason.trim();
    if (!reason) {
      this.errorMessage = 'A reason is required to reject a match.';
      return;
    }

    this.pendingActionMatchId = match.matchId;
    this.rejectingMatchId = null;
    // Optimistic removal from the pending list.
    this.matches = this.matches.filter((m) => m.matchId !== match.matchId);

    const decidedBy = this.authService.getCoordinatorId();
    this.matchesService.reject(match.matchId, decidedBy, reason).subscribe({
      next: () => {
        this.pendingActionMatchId = null;
        this.fetchPending();
      },
      error: () => {
        this.errorMessage = `Failed to reject match ${this.shortId(match.matchId)}.`;
        this.pendingActionMatchId = null;
        this.fetchPending();
      }
    });
  }

  shortId(id: string): string {
    return id.length > 8 ? `${id.slice(0, 8)}…` : id;
  }

  formatHours(hours: number): string {
    return `${hours.toFixed(1)}h`;
  }
}
