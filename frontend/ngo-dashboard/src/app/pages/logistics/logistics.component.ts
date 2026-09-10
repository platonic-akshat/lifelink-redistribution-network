import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { LogisticsService } from '../../core/logistics.service';
import { Logistics } from '../../models/logistics.model';

@Component({
  selector: 'app-logistics',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './logistics.component.html',
  styleUrl: './logistics.component.css'
})
export class LogisticsComponent implements OnInit {
  records: Logistics[] = [];
  isLoading = false;
  errorMessage: string | null = null;

  /** matchId currently mid-flight on a status-advance action */
  pendingActionMatchId: string | null = null;

  constructor(private readonly logisticsService: LogisticsService) {}

  ngOnInit(): void {
    this.fetchAll();
  }

  fetchAll(): void {
    const matchIds = this.logisticsService.getApprovedMatchIds();

    if (matchIds.length === 0) {
      this.records = [];
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;

    const requests = matchIds.map((matchId) =>
      this.logisticsService.get(matchId).pipe(catchError(() => of(null)))
    );

    forkJoin(requests).subscribe({
      next: (results) => {
        this.records = results.filter((r): r is Logistics => r !== null);
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load logistics records.';
        this.isLoading = false;
      }
    });
  }

  markPickedUp(matchId: string): void {
    this.runAction(matchId, () => this.logisticsService.markPickedUp(matchId));
  }

  markDelivered(matchId: string): void {
    this.runAction(matchId, () => this.logisticsService.markDelivered(matchId));
  }

  markFailed(matchId: string): void {
    this.runAction(matchId, () => this.logisticsService.markFailed(matchId));
  }

  private runAction(matchId: string, action: () => ReturnType<LogisticsService['markPickedUp']>): void {
    this.pendingActionMatchId = matchId;
    action().subscribe({
      next: () => {
        this.pendingActionMatchId = null;
        this.fetchAll();
      },
      error: () => {
        this.errorMessage = `Failed to update logistics for match ${this.shortId(matchId)}.`;
        this.pendingActionMatchId = null;
      }
    });
  }

  shortId(id: string): string {
    return id.length > 8 ? `${id.slice(0, 8)}…` : id;
  }
}
