import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { DashboardService, Alert, ElderlyOption } from '../../core/services/dashboard.service';
import { NotificationService } from '../../core/services/notification.service';
import { AuthService } from '../../core/services/auth.service';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-alerts',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule
  ],
  template: `
    <div class="p-6 max-w-5xl mx-auto space-y-6">
      <div class="flex items-center justify-between">
        <h1 class="text-2xl font-bold text-gray-800">All Alerts</h1>
      </div>

      <mat-card *ngIf="canManageAlerts" class="shadow-sm">
        <mat-card-content class="p-6 space-y-4">
          <h2 class="text-lg font-semibold text-gray-800">Create alert</h2>
          <form [formGroup]="createForm" (ngSubmit)="createAlert()" class="grid grid-cols-1 md:grid-cols-2 gap-4 items-end">
            <mat-form-field appearance="outline">
              <mat-label>Elderly person</mat-label>
              <mat-select formControlName="elderlyPersonId" required>
                <mat-option *ngFor="let e of elderlyOptions" [value]="e.id"
                  >{{ e.firstName }} {{ e.lastName }}</mat-option
                >
              </mat-select>
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Type</mat-label>
              <mat-select formControlName="alertType" required>
                <mat-option value="SOS">SOS</mat-option>
                <mat-option value="MEDICAL_EMERGENCY">Medical emergency</mat-option>
                <mat-option value="FALL_DETECTED">Fall detected</mat-option>
              </mat-select>
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Priority</mat-label>
              <mat-select formControlName="priority" required>
                <mat-option *ngFor="let p of priorityValues" [value]="p">{{ p }}</mat-option>
              </mat-select>
            </mat-form-field>
            <mat-form-field appearance="outline" class="md:col-span-2">
              <mat-label>Description</mat-label>
              <textarea matInput formControlName="description" rows="2" required></textarea>
            </mat-form-field>
            <div>
              <button mat-flat-button color="primary" type="submit" [disabled]="createForm.invalid || isSaving">
                {{ isSaving ? 'Saving...' : 'Add alert' }}
              </button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>

      <mat-card class="shadow-sm">
        <mat-card-content class="p-6 flex flex-wrap gap-4 items-end">
          <mat-form-field appearance="outline" class="w-44">
            <mat-label>Status</mat-label>
            <mat-select [(ngModel)]="resolvedFilter" [ngModelOptions]="{ standalone: true }" (selectionChange)="applyFilters()">
              <mat-option value="all">All</mat-option>
              <mat-option value="open">Open</mat-option>
              <mat-option value="resolved">Resolved</mat-option>
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline" class="w-44">
            <mat-label>Priority</mat-label>
            <mat-select [(ngModel)]="priorityServer" [ngModelOptions]="{ standalone: true }" (selectionChange)="applyFilters()">
              <mat-option value="">Any</mat-option>
              <mat-option *ngFor="let p of priorityValues" [value]="p">{{ p }}</mat-option>
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline" class="w-44">
            <mat-label>From</mat-label>
            <input matInput type="date" [(ngModel)]="fromDate" [ngModelOptions]="{ standalone: true }" />
          </mat-form-field>
          <mat-form-field appearance="outline" class="w-44">
            <mat-label>To</mat-label>
            <input matInput type="date" [(ngModel)]="toDate" [ngModelOptions]="{ standalone: true }" />
          </mat-form-field>
          <button mat-stroked-button color="primary" type="button" (click)="applyFilters()">Apply filters</button>
        </mat-card-content>
      </mat-card>

      <div *ngIf="isLoading" class="flex justify-center p-8">
        <mat-spinner diameter="40"></mat-spinner>
      </div>

      <mat-card *ngIf="!isLoading && alerts.length === 0" class="shadow-sm">
        <mat-card-content class="p-12 text-center text-gray-400">
          <mat-icon class="text-6xl mb-4 opacity-50">data_array</mat-icon>
          <p class="text-lg">No alerts found for these filters.</p>
        </mat-card-content>
      </mat-card>

      <div *ngIf="!isLoading && alerts.length > 0" class="grid gap-4">
        <mat-card
          *ngFor="let alert of alerts"
          class="shadow-sm hover:shadow-md transition-shadow"
          [ngClass]="{
            'border-l-4 border-red-500': !alert.isResolved,
            'border-l-4 border-green-500': alert.isResolved
          }"
        >
          <mat-card-content class="p-4 space-y-4">
            <div class="flex items-center justify-between gap-4 flex-wrap">
              <div class="flex items-center space-x-4">
                <div
                  class="w-12 h-12 rounded-full flex items-center justify-center flex-shrink-0"
                  [ngClass]="alert.isResolved ? 'bg-green-100 text-green-600' : 'bg-red-100 text-red-600'"
                >
                  <mat-icon>{{ alert.isResolved ? 'check_circle' : 'warning' }}</mat-icon>
                </div>
                <div>
                  <div class="flex flex-wrap items-center gap-2">
                    <h3 class="font-bold text-gray-800 text-lg">{{ alert.alertType }}</h3>
                    <span
                      class="px-2 py-0.5 rounded text-xs font-bold uppercase tracking-wide"
                      [ngClass]="priorityChipClass(alert)"
                      >{{ effectivePriority(alert) }}</span
                    >
                  </div>
                  <p class="text-sm text-gray-500">{{ alert.description }}</p>
                  <p class="text-xs text-gray-400 mt-1">
                    Elderly: {{ alert.elderlyPerson?.firstName }} {{ alert.elderlyPerson?.lastName }} ·
                    {{ alert.timestamp | date: 'medium' }}
                  </p>
                  <p *ngIf="alert.isResolved && alert.resolvedAt" class="text-xs text-gray-500 mt-1">
                    Resolved {{ alert.resolvedAt | date: 'medium' }}
                    <span *ngIf="alert.resolvedByEmail"> · {{ alert.resolvedByEmail }}</span>
                  </p>
                </div>
              </div>
              <div class="flex flex-col items-end space-y-2">
                <span
                  class="px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider"
                  [ngClass]="alert.isResolved ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'"
                >
                  {{ alert.isResolved ? 'Resolved' : 'Open' }}
                </span>
                <button
                  *ngIf="canManageAlerts && !alert.isResolved"
                  mat-stroked-button
                  color="primary"
                  class="rounded-full"
                  (click)="resolveAlert(alert.id)"
                >
                  Mark as Resolved
                </button>
              </div>
            </div>

            <div *ngIf="canManageAlerts && editingId === alert.id" class="border-t pt-4 grid grid-cols-1 md:grid-cols-2 gap-4" [formGroup]="editForm">
              <mat-form-field appearance="outline">
                <mat-label>Type</mat-label>
                <mat-select formControlName="alertType">
                  <mat-option value="SOS">SOS</mat-option>
                  <mat-option value="MEDICAL_EMERGENCY">Medical emergency</mat-option>
                  <mat-option value="FALL_DETECTED">Fall detected</mat-option>
                </mat-select>
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Priority</mat-label>
                <mat-select formControlName="priority">
                  <mat-option *ngFor="let p of priorityValues" [value]="p">{{ p }}</mat-option>
                </mat-select>
              </mat-form-field>
              <mat-form-field appearance="outline" class="md:col-span-2">
                <mat-label>Description</mat-label>
                <textarea matInput formControlName="description" rows="2"></textarea>
              </mat-form-field>
              <mat-form-field appearance="outline">
                <mat-label>Status</mat-label>
                <mat-select formControlName="isResolved">
                  <mat-option [value]="false">Open</mat-option>
                  <mat-option [value]="true">Resolved</mat-option>
                </mat-select>
              </mat-form-field>
              <div class="flex gap-2 items-center md:col-span-2">
                <button mat-flat-button color="primary" type="button" (click)="saveEdit(alert.id)" [disabled]="isSaving">Save changes</button>
                <button mat-button type="button" (click)="editingId = null">Cancel</button>
              </div>
            </div>
            <div *ngIf="canManageAlerts && editingId !== alert.id">
              <button mat-button color="primary" (click)="startEdit(alert)">Edit</button>
            </div>
          </mat-card-content>
        </mat-card>
      </div>

      <div *ngIf="!isLoading && totalPages > 1" class="flex flex-wrap items-center justify-between gap-4 pt-2">
        <p class="text-sm text-gray-600">{{ totalElements }} alert(s) total</p>
        <div class="flex items-center gap-2">
          <button mat-button type="button" [disabled]="page <= 0" (click)="goPrev()">Previous</button>
          <span class="text-sm text-gray-600">Page {{ page + 1 }} of {{ totalPages }}</span>
          <button mat-button type="button" [disabled]="page >= totalPages - 1" (click)="goNext()">Next</button>
        </div>
      </div>
    </div>
  `
})
export class AlertsComponent implements OnInit {
  private dashboardService = inject(DashboardService);
  private notifications = inject(NotificationService);
  private auth = inject(AuthService);
  private fb = inject(FormBuilder);

  readonly priorityValues = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'] as const;

  alerts: Alert[] = [];
  elderlyOptions: ElderlyOption[] = [];
  resolvedFilter: 'all' | 'open' | 'resolved' = 'all';
  priorityServer = '';
  fromDate = '';
  toDate = '';
  page = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  isLoading = true;
  isSaving = false;
  editingId: number | null = null;
  canManageAlerts = false;

  createForm: FormGroup = this.fb.group({
    elderlyPersonId: [null as number | null, Validators.required],
    alertType: ['MEDICAL_EMERGENCY', Validators.required],
    priority: ['MEDIUM', Validators.required],
    description: ['', Validators.required]
  });

  editForm: FormGroup = this.fb.group({
    alertType: ['', Validators.required],
    priority: ['MEDIUM', Validators.required],
    description: ['', Validators.required],
    isResolved: [false]
  });

  effectivePriority(a: Alert): string {
    return a.priority ?? 'MEDIUM';
  }

  priorityChipClass(a: Alert): Record<string, boolean> {
    const p = this.effectivePriority(a);
    return {
      'bg-slate-100 text-slate-700': p === 'LOW',
      'bg-blue-100 text-blue-800': p === 'MEDIUM',
      'bg-amber-100 text-amber-900': p === 'HIGH',
      'bg-red-100 text-red-800': p === 'URGENT'
    };
  }

  ngOnInit(): void {
    const r = this.auth.getRole();
    this.canManageAlerts = r === 'ADMIN' || r === 'CAREGIVER';
    if (this.canManageAlerts) {
      this.loadElderly();
    }
    this.loadAlerts();
  }

  loadElderly(): void {
    this.dashboardService.getElderlyPersons().subscribe({
      next: (list) => (this.elderlyOptions = list),
      error: () => this.notifications.error('Could not load elderly persons for alert form.')
    });
  }

  applyFilters(): void {
    this.page = 0;
    this.loadAlerts();
  }

  goPrev(): void {
    if (this.page > 0) {
      this.page--;
      this.loadAlerts();
    }
  }

  goNext(): void {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.loadAlerts();
    }
  }

  loadAlerts(): void {
    this.isLoading = true;
    const resolved =
      this.resolvedFilter === 'all' ? undefined : this.resolvedFilter === 'resolved';
    this.dashboardService
      .getAllAlertsPaged({
        page: this.page,
        size: this.pageSize,
        resolved,
        priority: this.priorityServer || undefined,
        from: this.fromDate ? `${this.fromDate}T00:00:00` : undefined,
        to: this.toDate ? `${this.toDate}T23:59:59` : undefined
      })
      .subscribe({
        next: (res) => {
          this.alerts = res.content;
          this.totalElements = res.totalElements;
          this.totalPages = Math.max(1, res.totalPages);
          this.isLoading = false;
        },
        error: () => {
          this.notifications.error('Could not load alerts.');
          this.isLoading = false;
        }
      });
  }

  createAlert(): void {
    if (this.createForm.invalid) {
      return;
    }
    this.isSaving = true;
    const v = this.createForm.value;
    this.dashboardService
      .createAlert({
        elderlyPersonId: v.elderlyPersonId,
        alertType: v.alertType,
        description: v.description,
        priority: v.priority
      })
      .subscribe({
        next: () => {
          this.createForm.reset({
            alertType: 'MEDICAL_EMERGENCY',
            priority: 'MEDIUM',
            elderlyPersonId: null,
            description: ''
          });
          this.applyFilters();
          this.isSaving = false;
          this.notifications.success('Alert created.');
        },
        error: (err: HttpErrorResponse) => {
          const msg =
            err.error && typeof err.error.message === 'string' ? err.error.message : 'Failed to create alert';
          this.notifications.error(msg);
          this.isSaving = false;
        }
      });
  }

  startEdit(alert: Alert): void {
    this.editingId = alert.id;
    this.editForm.patchValue({
      alertType: alert.alertType,
      priority: this.effectivePriority(alert),
      description: alert.description,
      isResolved: alert.isResolved
    });
  }

  saveEdit(id: number): void {
    if (this.editForm.invalid) {
      return;
    }
    this.isSaving = true;
    const v = this.editForm.value;
    this.dashboardService
      .updateAlert(id, {
        alertType: v.alertType,
        description: v.description,
        isResolved: v.isResolved,
        priority: v.priority
      })
      .subscribe({
        next: () => {
          this.editingId = null;
          this.loadAlerts();
          this.isSaving = false;
          this.notifications.success('Alert updated.');
        },
        error: (err: HttpErrorResponse) => {
          const msg =
            err.error && typeof err.error.message === 'string' ? err.error.message : 'Failed to update alert';
          this.notifications.error(msg);
          this.isSaving = false;
        }
      });
  }

  resolveAlert(id: number): void {
    this.dashboardService.resolveAlert(id).subscribe({
      next: () => {
        this.loadAlerts();
        this.notifications.success('Alert marked as resolved.');
      },
      error: () => this.notifications.error('Failed to resolve alert.')
    });
  }
}
