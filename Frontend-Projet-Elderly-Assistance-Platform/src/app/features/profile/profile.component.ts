import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService, User } from '../../core/services/user.service';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatProgressSpinnerModule, MatIconModule],
  template: `
    <div class="p-6 max-w-3xl mx-auto">
      <h1 class="text-2xl font-bold mb-6 text-gray-800">My Profile</h1>
      
      <div *ngIf="isLoading" class="flex justify-center p-8">
        <mat-spinner diameter="40"></mat-spinner>
      </div>

      <mat-card *ngIf="!isLoading && user" class="shadow-md">
        <mat-card-content class="p-6">
          <div class="flex items-center space-x-6">
            <div class="w-24 h-24 bg-primary text-white rounded-full flex items-center justify-center text-4xl font-bold">
              {{ user.firstName[0] }}{{ user.lastName[0] }}
            </div>
            <div>
              <h2 class="text-2xl font-bold text-gray-800">{{ user.firstName }} {{ user.lastName }}</h2>
              <p class="text-gray-500 flex items-center mt-1">
                <mat-icon class="mr-1 text-sm w-4 h-4">email</mat-icon> {{ user.email }}
              </p>
              <div class="mt-3 inline-block px-3 py-1 bg-blue-100 text-primary text-sm font-semibold rounded-full items-center">
                <mat-icon class="w-4 h-4 text-sm inline align-middle mr-1">badge</mat-icon> {{ user.role }}
              </div>
            </div>
          </div>
        </mat-card-content>
      </mat-card>
      
      <div *ngIf="!isLoading && !user" class="text-red-500 p-4 bg-red-50 rounded-md">
        Failed to load profile data.
      </div>
    </div>
  `
})
export class ProfileComponent implements OnInit {
  private userService = inject(UserService);
  user: User | null = null;
  isLoading = true;

  ngOnInit() {
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.user = user;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load profile', err);
        this.isLoading = false;
      }
    });
  }
}

