import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule, MatListModule, MatIconModule],
  template: `
    <div class="w-64 h-full bg-white shadow-xl flex flex-col border-r border-gray-100">
      <div class="p-6">
        <p class="text-xs uppercase tracking-wider text-gray-400 font-semibold mb-2">Main Menu</p>
      </div>
      
      <mat-nav-list class="px-2">
        <a mat-list-item routerLink="/dashboard" routerLinkActive="bg-blue-50 text-primary border-r-4 border-primary shadow-sm" class="mb-1 rounded-lg transition-all text-gray-600 hover:bg-gray-50">
          <mat-icon matListItemIcon class="text-inherit">dashboard</mat-icon>
          <p matListItemTitle class="font-medium text-inherit">Dashboard</p>
        </a>
        <a *ngIf="(authService.userRole$ | async) === 'ADMIN'" mat-list-item routerLink="/users" routerLinkActive="bg-blue-50 text-primary border-r-4 border-primary shadow-sm" class="mb-1 rounded-lg transition-all text-gray-600 hover:bg-gray-50">
          <mat-icon matListItemIcon class="text-inherit">people</mat-icon>
          <p matListItemTitle class="font-medium text-inherit">Manage Users</p>
        </a>
        <a *ngIf="(authService.userRole$ | async) === 'ADMIN' || (authService.userRole$ | async) === 'CAREGIVER'" mat-list-item routerLink="/reports" routerLinkActive="bg-blue-50 text-primary border-r-4 border-primary shadow-sm" class="mb-1 rounded-lg transition-all text-gray-600 hover:bg-gray-50">
          <mat-icon matListItemIcon class="text-inherit">bar_chart</mat-icon>
          <p matListItemTitle class="font-medium text-inherit">Reports</p>
        </a>
        <a mat-list-item routerLink="/alerts" routerLinkActive="bg-blue-50 text-primary border-r-4 border-primary shadow-sm" class="mb-1 rounded-lg transition-all text-gray-600 hover:bg-gray-50">
          <mat-icon matListItemIcon class="text-inherit">notifications_active</mat-icon>
          <p matListItemTitle class="font-medium text-inherit">Alerts</p>
        </a>
        <a *ngIf="(authService.userRole$ | async) === 'ADMIN' || (authService.userRole$ | async) === 'FAMILY_MEMBER'" mat-list-item routerLink="/family-timeline" routerLinkActive="bg-blue-50 text-primary border-r-4 border-primary shadow-sm" class="mb-1 rounded-lg transition-all text-gray-600 hover:bg-gray-50">
          <mat-icon matListItemIcon class="text-inherit">history_toggle_off</mat-icon>
          <p matListItemTitle class="font-medium text-inherit">Timeline</p>
        </a>
        <a mat-list-item routerLink="/elderly-persons" routerLinkActive="bg-blue-50 text-primary border-r-4 border-primary shadow-sm" class="mb-1 rounded-lg transition-all text-gray-600 hover:bg-gray-50">
          <mat-icon matListItemIcon class="text-inherit">elderly</mat-icon>
          <p matListItemTitle class="font-medium text-inherit">Elderly</p>
        </a>
        <a mat-list-item routerLink="/profile" routerLinkActive="bg-blue-50 text-primary border-r-4 border-primary shadow-sm" class="mb-1 rounded-lg transition-all text-gray-600 hover:bg-gray-50">
          <mat-icon matListItemIcon class="text-inherit">person</mat-icon>
          <p matListItemTitle class="font-medium text-inherit">My Profile</p>
        </a>
      </mat-nav-list>

      <div class="mt-auto p-4 m-4 bg-blue-50 rounded-xl relative overflow-hidden">
        <div class="absolute -right-4 -top-4 w-16 h-16 bg-primary opacity-10 rounded-full"></div>
        <mat-icon class="text-primary mb-2">help_outline</mat-icon>
        <p class="text-sm font-medium text-gray-800">Support Center</p>
        <p class="text-xs text-gray-500 mt-1">Need help managing the elderly module?</p>
      </div>
    </div>
  `,
  styles: [`
    .mdc-list-item { border-radius: 8px !important; margin-bottom: 4px; }
  `]
})
export class SidebarComponent {
    constructor(public authService: AuthService) {}
}
