import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DashboardService, ReportSummary } from '../../core/services/dashboard.service';
import { NotificationService } from '../../core/services/notification.service';
import { AuthService } from '../../core/services/auth.service';
import { UserService, User } from '../../core/services/user.service';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatTableModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule
  ],
  template: `
    <div class="p-6 max-w-5xl mx-auto space-y-6">
      <div class="flex flex-wrap items-center justify-between gap-4">
        <h1 class="text-2xl font-bold text-gray-800">System Reports</h1>
        <div class="flex flex-wrap gap-2">
          <button mat-stroked-button color="primary" type="button" (click)="downloadCsv()" [disabled]="isLoading || !reportData">
            <mat-icon class="mr-1">file_download</mat-icon>
            Export CSV
          </button>
          <button mat-flat-button color="primary" type="button" (click)="loadReport()" [disabled]="isLoading">
            <mat-icon class="mr-1">refresh</mat-icon>
            Refresh
          </button>
        </div>
      </div>

      <mat-card class="shadow-sm">
        <mat-card-content class="p-6 flex flex-wrap gap-4 items-end">
          <mat-form-field appearance="outline" class="w-44">
            <mat-label>From</mat-label>
            <input matInput type="date" [(ngModel)]="fromStr" />
          </mat-form-field>
          <mat-form-field appearance="outline" class="w-44">
            <mat-label>To</mat-label>
            <input matInput type="date" [(ngModel)]="toStr" />
          </mat-form-field>
          <mat-form-field *ngIf="isAdmin" appearance="outline" class="min-w-[14rem]">
            <mat-label>Caregiver (optional)</mat-label>
            <mat-select [(ngModel)]="caregiverFilter">
              <mat-option [value]="null">All caregivers</mat-option>
              <mat-option *ngFor="let c of caregivers" [value]="c.id">{{ c.firstName }} {{ c.lastName }}</mat-option>
            </mat-select>
          </mat-form-field>
          <button mat-stroked-button color="primary" type="button" (click)="loadReport()">Apply range</button>
        </mat-card-content>
      </mat-card>

      <div *ngIf="isLoading" class="flex justify-center p-8">
        <mat-spinner diameter="40"></mat-spinner>
      </div>

      <mat-card *ngIf="!isLoading && reportData" class="shadow-md border-t-4 border-primary">
        <mat-card-header class="border-b border-gray-100 p-4 bg-gray-50">
          <mat-card-title class="text-lg font-bold text-gray-700">Platform analytics</mat-card-title>
          <p class="text-sm text-gray-500 mt-1" *ngIf="reportData.from && reportData.to">
            Range: {{ reportData.from }} — {{ reportData.to }}
          </p>
        </mat-card-header>
        <mat-card-content class="p-6 space-y-8">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="p-4 bg-blue-50 rounded-lg flex items-center justify-between">
              <div>
                <p class="text-sm font-semibold text-blue-600 uppercase">Total elderly persons</p>
                <h2 class="text-3xl font-bold text-gray-800">{{ reportData.totalElderly }}</h2>
              </div>
              <mat-icon class="text-blue-300 text-5xl" style="height: 48px; width: 48px">elderly</mat-icon>
            </div>
            <div class="p-4 bg-green-50 rounded-lg flex items-center justify-between">
              <div>
                <p class="text-sm font-semibold text-green-600 uppercase">Caregivers (scope)</p>
                <h2 class="text-3xl font-bold text-gray-800">{{ reportData.totalCaregivers }}</h2>
              </div>
              <mat-icon class="text-green-300 text-5xl" style="height: 48px; width: 48px">medical_services</mat-icon>
            </div>
            <div class="p-4 bg-red-50 rounded-lg flex items-center justify-between md:col-span-2">
              <div>
                <p class="text-sm font-semibold text-red-600 uppercase">Unresolved alerts (global scope)</p>
                <h2 class="text-3xl font-bold text-gray-800">{{ reportData.unresolvedAlerts || 0 }}</h2>
              </div>
              <mat-icon class="text-red-300 text-5xl" style="height: 48px; width: 48px">warning</mat-icon>
            </div>
          </div>

          <div>
            <h3 class="text-md font-semibold text-gray-700 mb-2">Alerts in range</h3>
            <p class="text-2xl font-bold text-gray-800">{{ reportData.alertsInRangeCount ?? 0 }}</p>
          </div>

          <div *ngIf="chartBars().length > 0" class="border-t border-gray-100 pt-6">
            <h3 class="text-md font-semibold text-gray-700 mb-4">Alerts by day</h3>
            <div class="flex items-end gap-1 h-48 border-b border-gray-200 pb-1">
              <div *ngFor="let bar of chartBars()" class="flex-1 min-w-0 flex flex-col items-center gap-1 group">
                <div
                  class="w-full max-w-[2rem] mx-auto rounded-t bg-primary/80 hover:bg-primary transition-colors"
                  [style.height.%]="bar.pct"
                  [title]="bar.day + ': ' + bar.count"
                ></div>
                <span class="text-[10px] text-gray-400 truncate w-full text-center">{{ bar.day.slice(5) }}</span>
              </div>
            </div>
          </div>
          <p *ngIf="chartBars().length === 0" class="text-sm text-gray-500">No per-day alert data for this range.</p>
        </mat-card-content>
      </mat-card>

      <div *ngIf="!isLoading && !reportData" class="p-8 text-center text-red-500 bg-red-50 rounded-lg">
        Failed to load report data. Check your permissions or try again.
      </div>
    </div>
  `
})
export class ReportsComponent implements OnInit {
  private dashboardService = inject(DashboardService);
  private notifications = inject(NotificationService);
  private auth = inject(AuthService);
  private userService = inject(UserService);

  isLoading = true;
  reportData: ReportSummary | null = null;
  isAdmin = false;
  fromStr = '';
  toStr = '';
  caregiverFilter: number | null = null;
  caregivers: User[] = [];

  ngOnInit(): void {
    this.isAdmin = this.auth.getRole() === 'ADMIN';
    if (this.isAdmin) {
      this.userService.getAllUsers().subscribe({
        next: (users) => (this.caregivers = users.filter((u) => u.role === 'CAREGIVER')),
        error: () => this.notifications.error('Could not load caregivers for filter.')
      });
    }
    this.loadReport();
  }

  chartBars(): { day: string; count: number; pct: number }[] {
    const byDay = this.reportData?.alertsByDay;
    if (!byDay || typeof byDay !== 'object') {
      return [];
    }
    const entries = Object.entries(byDay).sort(([a], [b]) => a.localeCompare(b));
    const max = Math.max(1, ...entries.map(([, c]) => Number(c) || 0));
    return entries.map(([day, count]) => ({
      day,
      count: Number(count) || 0,
      pct: ((Number(count) || 0) / max) * 100
    }));
  }

  private currentFilters(): { from?: string; to?: string; caregiverId?: number } {
    const f: { from?: string; to?: string; caregiverId?: number } = {};
    if (this.fromStr) {
      f.from = this.fromStr;
    }
    if (this.toStr) {
      f.to = this.toStr;
    }
    if (this.isAdmin && this.caregiverFilter != null) {
      f.caregiverId = this.caregiverFilter;
    }
    return f;
  }

  loadReport(): void {
    this.isLoading = true;
    this.dashboardService.getReportSummary(this.currentFilters()).subscribe({
      next: (data) => {
        this.reportData = {
          ...data,
          alertsByDay: data.alertsByDay ?? {}
        };
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load report', err);
        this.notifications.error('Could not load report. Check your permissions or try again.');
        this.reportData = null;
        this.isLoading = false;
      }
    });
  }

  downloadCsv(): void {
    this.dashboardService.downloadReportCsv(this.currentFilters()).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'report-summary.csv';
        a.click();
        URL.revokeObjectURL(url);
        this.notifications.success('CSV download started.');
      },
      error: () => this.notifications.error('Could not download CSV.')
    });
  }
}
