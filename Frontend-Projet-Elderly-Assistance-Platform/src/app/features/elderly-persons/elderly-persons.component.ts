import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { UserService, User } from '../../core/services/user.service';
import { NotificationService } from '../../core/services/notification.service';

export interface ElderlyPerson {
  id: number;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  address?: string;
  medicalConditions: string;
  careStatus?: string;
  caregiver?: {
    id?: number;
    firstName: string;
    lastName: string;
  };
}

@Component({
  selector: 'app-elderly-persons',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatTableModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatButtonModule,
    MatFormFieldModule,
    MatFormFieldModule,
    MatSelectModule,
    RouterModule
  ],
  template: `
    <div class="p-6">
      <h1 class="text-2xl font-bold mb-6 text-gray-800">Elderly Persons</h1>

      <div *ngIf="isLoading" class="flex justify-center p-8">
        <mat-spinner diameter="40"></mat-spinner>
      </div>

      <mat-card *ngIf="!isLoading">
        <table mat-table [dataSource]="elderlyPersons" class="w-full">
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef> Name </th>
            <td mat-cell *matCellDef="let element"> {{element.firstName}} {{element.lastName}} </td>
          </ng-container>

          <ng-container matColumnDef="dateOfBirth">
            <th mat-header-cell *matHeaderCellDef> Date of Birth </th>
            <td mat-cell *matCellDef="let element"> {{element.dateOfBirth | date}} </td>
          </ng-container>

          <ng-container matColumnDef="medicalConditions">
            <th mat-header-cell *matHeaderCellDef> Conditions </th>
            <td mat-cell *matCellDef="let element"> {{element.medicalConditions || 'None'}} </td>
          </ng-container>

          <ng-container matColumnDef="careStatus">
            <th mat-header-cell *matHeaderCellDef> Care status </th>
            <td mat-cell *matCellDef="let element">
              <span
                class="px-3 py-1 rounded-full text-xs font-semibold uppercase"
                [ngClass]="careStatusClass(element.careStatus)"
              >
                {{ element.careStatus || 'STABLE' }}
              </span>
              @if (canEditCareStatus) {
                <div class="mt-2 flex flex-wrap items-center gap-2">
                  <mat-form-field appearance="outline" class="w-40 min-w-[10rem]">
                    <mat-label>Update</mat-label>
                    <mat-select (selectionChange)="selectedCareStatus[element.id] = $event.value">
                      <mat-option *ngFor="let s of careStatusValues" [value]="s">{{ s }}</mat-option>
                    </mat-select>
                  </mat-form-field>
                  <button
                    mat-stroked-button
                    color="primary"
                    type="button"
                    (click)="updateCareStatus(element.id)"
                    [disabled]="!selectedCareStatus[element.id]"
                  >
                    Apply
                  </button>
                </div>
              }
            </td>
          </ng-container>

          <ng-container matColumnDef="caregiver">
            <th mat-header-cell *matHeaderCellDef> Caregiver </th>
            <td mat-cell *matCellDef="let element">
               <span *ngIf="element.caregiver" class="px-3 py-1 bg-green-50 text-green-700 rounded-full text-xs font-semibold">
                 {{element.caregiver.firstName}} {{element.caregiver.lastName}}
               </span>
               <span *ngIf="!element.caregiver" class="text-gray-400 text-xs italic">Unassigned</span>
            </td>
          </ng-container>

          <ng-container matColumnDef="medications">
            <th mat-header-cell *matHeaderCellDef> Actions </th>
            <td mat-cell *matCellDef="let element">
              <a mat-icon-button color="primary" [routerLink]="['/medications', element.id]" title="Medications">
                <mat-icon>medical_services</mat-icon>
              </a>
              <a mat-icon-button style="color: #6366f1;" [routerLink]="['/geofence', element.id]" title="Geofence Simulator">
                <mat-icon>satellite_alt</mat-icon>
              </a>
              <button mat-icon-button color="accent" (click)="downloadPdf(element)" title="Export PDF">
                <mat-icon>picture_as_pdf</mat-icon>
              </button>
            </td>
          </ng-container>

          @if (isAdmin) {
            <ng-container matColumnDef="assign">
              <th mat-header-cell *matHeaderCellDef> Reassign caregiver </th>
              <td mat-cell *matCellDef="let element">
                <div class="flex flex-wrap items-center gap-2">
                  <mat-form-field appearance="outline" class="w-48 min-w-[12rem]">
                    <mat-label>Caregiver</mat-label>
                    <mat-select (selectionChange)="selectedCaregiver[element.id] = $event.value">
                      <mat-option *ngFor="let c of caregiverUsers" [value]="c.id">{{ c.firstName }} {{ c.lastName }}</mat-option>
                    </mat-select>
                  </mat-form-field>
                  <button mat-stroked-button color="primary" type="button"
                          (click)="assignCaregiver(element.id)"
                          [disabled]="!selectedCaregiver[element.id]">
                    Apply
                  </button>
                </div>
              </td>
            </ng-container>
          }

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
        </table>

        <div *ngIf="elderlyPersons.length === 0" class="p-8 text-center text-gray-500">
          No elderly persons found in the system.
        </div>
      </mat-card>
    </div>
  `,
  styles: [`
    table { width: 100%; }
    .mat-mdc-table { box-shadow: none !important; }
  `]
})
export class ElderlyPersonsComponent implements OnInit {
  private http = inject(HttpClient);
  private auth = inject(AuthService);
  private userService = inject(UserService);
  private notifications = inject(NotificationService);

  elderlyPersons: ElderlyPerson[] = [];
  caregiverUsers: User[] = [];
  displayedColumns: string[] = ['name', 'dateOfBirth', 'medicalConditions', 'careStatus', 'caregiver', 'medications'];
  isLoading = true;
  isAdmin = false;
  canEditCareStatus = false;
  readonly careStatusValues = ['STABLE', 'WARNING', 'CRITICAL'] as const;
  selectedCaregiver: Record<number, number> = {};
  selectedCareStatus: Record<number, string> = {};

  ngOnInit() {
    const role = this.auth.getRole();
    this.isAdmin = role === 'ADMIN';
    this.canEditCareStatus = role === 'ADMIN' || role === 'CAREGIVER';
    if (this.isAdmin) {
      this.displayedColumns = [...this.displayedColumns, 'assign'];
      this.userService.getAllUsers().subscribe({
        next: (users) => (this.caregiverUsers = users.filter((u) => u.role === 'CAREGIVER')),
        error: (err) => console.error('Failed to load caregivers', err)
      });
    }
    this.http.get<ElderlyPerson[]>('/api/elderly-persons').subscribe({
      next: (data) => {
        this.elderlyPersons = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load elderly persons', err);
        this.isLoading = false;
      }
    });
  }

  careStatusClass(status: string | undefined): Record<string, boolean> {
    const s = (status ?? 'STABLE').toUpperCase();
    return {
      'bg-emerald-50 text-emerald-800': s === 'STABLE',
      'bg-amber-50 text-amber-900': s === 'WARNING',
      'bg-rose-50 text-rose-800': s === 'CRITICAL'
    };
  }

  updateCareStatus(elderlyId: number): void {
    const status = this.selectedCareStatus[elderlyId];
    if (!status) {
      return;
    }
    this.http.put<ElderlyPerson>(`/api/elderly-persons/${elderlyId}/care-status`, { careStatus: status }).subscribe({
      next: () => {
        this.notifications.success('Care status updated.');
        this.reloadElderly();
      },
      error: (err: HttpErrorResponse) => {
        const msg =
          err.error && typeof err.error.message === 'string' ? err.error.message : 'Could not update care status';
        this.notifications.error(msg);
      }
    });
  }

  private reloadElderly(): void {
    this.isLoading = true;
    this.http.get<ElderlyPerson[]>('/api/elderly-persons').subscribe({
      next: (data) => {
        this.elderlyPersons = data;
        this.isLoading = false;
      },
      error: () => (this.isLoading = false)
    });
  }

  assignCaregiver(elderlyId: number) {
    const caregiverId = this.selectedCaregiver[elderlyId];
    if (!caregiverId) {
      return;
    }
    this.http.put<ElderlyPerson>(`/api/elderly-persons/${elderlyId}/caregiver`, { caregiverId }).subscribe({
      next: () => {
        this.notifications.success('Caregiver assignment updated.');
        this.reloadElderly();
      },
      error: (err: HttpErrorResponse) => {
        const msg =
          err.error && typeof err.error.message === 'string' ? err.error.message : 'Assignment failed';
        this.notifications.error(msg);
      }
    });
  }

  downloadPdf(elderly: ElderlyPerson) {
    this.notifications.success('Generating PDF...');
    this.http.get(`/api/reports/elderly/${elderly.id}/export-pdf`, { responseType: 'blob' }).subscribe({
      next: (blob: Blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `medical_report_${elderly.lastName}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: () => this.notifications.error('Failed to generate report')
    });
  }
}
