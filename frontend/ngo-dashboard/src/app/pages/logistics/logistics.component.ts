import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
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
    this.isLoading = true;
    this.errorMessage = null;

    this.logisticsService.list().subscribe({
      next: (page) => {
        this.records = page.content ?? [];
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load logistics records.';
        this.isLoading = false;
      },
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
