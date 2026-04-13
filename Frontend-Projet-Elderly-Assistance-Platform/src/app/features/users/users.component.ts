import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { UserService, User } from '../../core/services/user.service';
import { NotificationService } from '../../core/services/notification.service';
import { DashboardService, ElderlyOption } from '../../core/services/dashboard.service';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatCardModule, MatTableModule,
    MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule, MatSelectModule
  ],
  template: `
    <div class="p-6 space-y-6">
      <div class="flex justify-between items-center">
        <h1 class="text-2xl font-bold text-gray-800">Manage Users</h1>
        <div class="flex gap-2">
          <button *ngIf="editingUserId !== null" mat-stroked-button type="button" (click)="cancelEdit()">Cancel edit</button>
          <button mat-flat-button color="primary" (click)="toggleForm()">
            <mat-icon>{{ showForm ? 'close' : 'add' }}</mat-icon> {{ showForm ? 'Close' : 'Add User' }}
          </button>
        </div>
      </div>

      <mat-card *ngIf="showForm" class="bg-gray-50 border border-gray-100">
        <mat-card-content class="p-6">
          <h2 class="text-lg font-semibold mb-4">{{ editingUserId !== null ? 'Edit user' : 'Create user' }}</h2>
          <form [formGroup]="userForm" (ngSubmit)="submitUser()" class="flex flex-col gap-4">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <mat-form-field appearance="outline">
                <mat-label>First Name</mat-label>
                <input matInput formControlName="firstName" required>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Last Name</mat-label>
                <input matInput formControlName="lastName" required>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Email</mat-label>
                <input matInput formControlName="email" type="email" required>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Password</mat-label>
                <input matInput formControlName="password" type="password">
                <mat-hint *ngIf="editingUserId !== null">Leave blank to keep current password</mat-hint>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Role</mat-label>
                <mat-select formControlName="role" required (selectionChange)="onRoleChange()">
                  <mat-option value="ADMIN">Admin</mat-option>
                  <mat-option value="CAREGIVER">Caregiver</mat-option>
                  <mat-option value="ELDERLY">Elderly</mat-option>
                  <mat-option value="FAMILY_MEMBER">Family Member</mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline" *ngIf="userForm.get('role')?.value === 'FAMILY_MEMBER'">
                <mat-label>Linked elderly person</mat-label>
                <mat-select formControlName="linkedElderlyPersonId" required>
                  <mat-option *ngFor="let e of elderlyOptions" [value]="e.id">
                    {{ e.firstName }} {{ e.lastName }} (ID {{ e.id }})
                  </mat-option>
                </mat-select>
                <mat-hint>Required for family members</mat-hint>
              </mat-form-field>

              <mat-form-field appearance="outline" *ngIf="userForm.get('role')?.value === 'ELDERLY'">
                <mat-label>Assign caregiver (optional)</mat-label>
                <mat-select formControlName="caregiverId">
                  <mat-option [value]="null">None (assign later)</mat-option>
                  <mat-option *ngFor="let c of caregiverUsers" [value]="c.id">
                    {{ c.firstName }} {{ c.lastName }}
                  </mat-option>
                </mat-select>
              </mat-form-field>
            </div>
            <div>
              <button mat-flat-button color="primary" type="submit" [disabled]="userForm.invalid || isSubmitting">
                {{ isSubmitting ? 'Saving...' : (editingUserId !== null ? 'Update user' : 'Create user') }}
              </button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>

      <mat-card>
        <table mat-table [dataSource]="users" class="w-full">
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef> Name </th>
            <td mat-cell *matCellDef="let user"> {{user.firstName}} {{user.lastName}} </td>
          </ng-container>

          <ng-container matColumnDef="email">
            <th mat-header-cell *matHeaderCellDef> Email </th>
            <td mat-cell *matCellDef="let user"> {{user.email}} </td>
          </ng-container>

          <ng-container matColumnDef="role">
            <th mat-header-cell *matHeaderCellDef> Role </th>
            <td mat-cell *matCellDef="let user">
              <span class="px-2 py-1 bg-blue-100 text-blue-800 text-xs font-semibold rounded-full uppercase">{{user.role}}</span>
            </td>
          </ng-container>

          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef> Actions </th>
            <td mat-cell *matCellDef="let user">
              <button mat-icon-button color="primary" (click)="startEdit(user)" title="Edit">
                <mat-icon>edit</mat-icon>
              </button>
              <button mat-icon-button color="warn" (click)="deleteUser(user.id!)">
                <mat-icon>delete</mat-icon>
              </button>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
        </table>
      </mat-card>
    </div>
  `,
  styles: [`
    table { width: 100%; }
    .mat-mdc-table { box-shadow: none !important; }
  `]
})
export class UsersComponent implements OnInit {
  private userService = inject(UserService);
  private dashboardService = inject(DashboardService);
  private notifications = inject(NotificationService);
  private fb = inject(FormBuilder);

  users: User[] = [];
  elderlyOptions: ElderlyOption[] = [];
  caregiverUsers: User[] = [];
  displayedColumns: string[] = ['name', 'email', 'role', 'actions'];

  showForm = false;
  isSubmitting = false;
  editingUserId: number | null = null;

  userForm: FormGroup = this.fb.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: [''],
    role: ['', Validators.required],
    linkedElderlyPersonId: [null as number | null],
    caregiverId: [null as number | null]
  });

  ngOnInit() {
    this.userForm.get('password')?.setValidators(Validators.required);
    this.userForm.get('password')?.updateValueAndValidity();
    this.loadUsers();
    this.loadElderlyOptions();
  }

  loadUsers() {
    this.userService.getAllUsers().subscribe({
      next: (users) => {
        this.users = users;
        this.caregiverUsers = users.filter((u) => u.role === 'CAREGIVER');
      },
      error: (err) => console.error('Failed to load users', err)
    });
  }

  loadElderlyOptions() {
    this.dashboardService.getElderlyPersons().subscribe({
      next: (list) => (this.elderlyOptions = list),
      error: (err) => console.error('Failed to load elderly list', err)
    });
  }

  onRoleChange() {
    const role = this.userForm.get('role')?.value;
    if (role !== 'FAMILY_MEMBER') {
      this.userForm.patchValue({ linkedElderlyPersonId: null });
    }
    if (role !== 'ELDERLY') {
      this.userForm.patchValue({ caregiverId: null });
    }
  }

  toggleForm() {
    this.showForm = !this.showForm;
    if (!this.showForm) {
      this.cancelEdit();
    } else {
      this.editingUserId = null;
      this.userForm.reset({ linkedElderlyPersonId: null, caregiverId: null });
      this.userForm.get('password')?.setValidators(Validators.required);
      this.userForm.get('password')?.updateValueAndValidity();
    }
  }

  startEdit(user: User) {
    this.showForm = true;
    this.editingUserId = user.id ?? null;
    this.userForm.get('password')?.clearValidators();
    this.userForm.get('password')?.updateValueAndValidity();
    this.userForm.patchValue({
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
      password: '',
      role: user.role,
      linkedElderlyPersonId: user.linkedElderlyPersonId ?? null,
      caregiverId: null
    });
  }

  cancelEdit() {
    this.editingUserId = null;
    this.userForm.reset({ linkedElderlyPersonId: null, caregiverId: null });
    this.userForm.get('password')?.setValidators(Validators.required);
    this.userForm.get('password')?.updateValueAndValidity();
  }

  submitUser() {
    if (this.userForm.invalid) {
      return;
    }
    const v = this.userForm.value;
    if (v.role === 'FAMILY_MEMBER' && (v.linkedElderlyPersonId === null || v.linkedElderlyPersonId === undefined)) {
      this.notifications.error('Please select the elderly person this family member is linked to.');
      return;
    }

    const body: Record<string, unknown> = {
      firstName: v.firstName,
      lastName: v.lastName,
      email: v.email,
      role: v.role,
      linkedElderlyPersonId: v.role === 'FAMILY_MEMBER' ? v.linkedElderlyPersonId : null,
      caregiverId: v.role === 'ELDERLY' ? v.caregiverId : null
    };
    if (this.editingUserId === null || (v.password && String(v.password).length > 0)) {
      body['password'] = v.password;
    }

    this.isSubmitting = true;
    const req$ =
      this.editingUserId !== null
        ? this.userService.updateUser(this.editingUserId, body)
        : this.userService.createUser(body);

    req$.subscribe({
      next: () => {
        this.loadUsers();
        this.loadElderlyOptions();
        this.isSubmitting = false;
        this.showForm = false;
        this.cancelEdit();
        this.notifications.success('User saved.');
      },
      error: (err: HttpErrorResponse) => {
        console.error('Save user failed', err);
        const msg =
          err.error && typeof err.error.message === 'string'
            ? err.error.message
            : 'Failed to save user';
        this.notifications.error(msg);
        this.isSubmitting = false;
      }
    });
  }

  deleteUser(id: number) {
    if (confirm('Are you sure you want to delete this user?')) {
      this.userService.deleteUser(id).subscribe({
        next: () => {
          this.loadUsers();
          this.notifications.success('User deleted.');
        },
        error: (err: HttpErrorResponse) => {
          console.error('Error deleting user', err);
          const msg =
            err.error && typeof err.error.message === 'string'
              ? err.error.message
              : 'Could not delete user';
          this.notifications.error(msg);
        }
      });
    }
  }
}
