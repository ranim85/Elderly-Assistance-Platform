import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterModule } from '@angular/router';
import { catchError, finalize, timeout } from 'rxjs/operators';
import { of } from 'rxjs';
import { DashboardService, DashboardStats, Alert } from '../../core/services/dashboard.service';
import { NotificationService } from '../../core/services/notification.service';
import { AuthService } from '../../core/services/auth.service';

const EMPTY_STATS: DashboardStats = {
  totalAssisted: 0,
  activeCaregivers: 0,
  urgentAlerts: 0,
  elderlyStable: 0,
  elderlyWarning: 0,
  elderlyCritical: 0
};

/** Fails hung API calls so loading flags and overlays cannot wedge forever. */
const DASHBOARD_HTTP_TIMEOUT_MS = 20_000;

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule, RouterModule],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  private dashboardService = inject(DashboardService);
  private notifications = inject(NotificationService);
  private authService = inject(AuthService);
  private destroyRef = inject(DestroyRef);

  stats: DashboardStats | null = null;
  recentAlerts: Alert[] = [];
  isLoadingStats = true;
  isLoadingAlerts = true;
  isGeneratingReport = false;

  canViewReports(): boolean {
    const r = this.authService.getRole();
    return r === 'ADMIN' || r === 'CAREGIVER';
  }

  ngOnInit(): void {
    this.loadStats();
    this.loadRecentAlerts();
  }

  loadStats(): void {
    this.isLoadingStats = true;
    this.dashboardService
      .getStats()
      .pipe(
        timeout(DASHBOARD_HTTP_TIMEOUT_MS),
        takeUntilDestroyed(this.destroyRef),
        catchError((err) => {
          console.error('Failed to load stats', err);
          this.notifications.error('Could not load dashboard statistics. Showing zeros.');
          return of(EMPTY_STATS);
        }),
        finalize(() => {
          this.isLoadingStats = false;
        })
      )
      .subscribe((data) => {
        this.stats = data;
      });
  }

  loadRecentAlerts(): void {
    this.isLoadingAlerts = true;
    this.dashboardService
      .getRecentAlerts()
      .pipe(
        timeout(DASHBOARD_HTTP_TIMEOUT_MS),
        takeUntilDestroyed(this.destroyRef),
        catchError((err) => {
          console.error('Failed to load recent alerts', err);
          this.notifications.error('Could not load recent alerts.');
          return of([] as Alert[]);
        }),
        finalize(() => {
          this.isLoadingAlerts = false;
        })
      )
      .subscribe((data) => {
        this.recentAlerts = data;
      });
  }

  generateReport(): void {
    if (!this.canViewReports()) {
      return;
    }
    this.isGeneratingReport = true;
    this.dashboardService
      .getReportSummary({})
      .pipe(
        timeout(DASHBOARD_HTTP_TIMEOUT_MS),
        takeUntilDestroyed(this.destroyRef),
        catchError((err) => {
          console.error('Failed to generate report', err);
          this.notifications.error('Failed to generate report.');
          return of(null);
        }),
        finalize(() => {
          this.isGeneratingReport = false;
        })
      )
      .subscribe((res) => {
        if (res) {
          this.notifications.success(
            `Report ready: ${res.totalElderly ?? 0} elderly, ${res.unresolvedAlerts ?? 0} open alerts.`
          );
        }
      });
  }
}
