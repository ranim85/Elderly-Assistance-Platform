import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule, MatToolbarModule, MatButtonModule, MatIconModule],
  template: `
    <mat-toolbar class="bg-white border-b border-gray-100 flex justify-between h-16 w-full px-6">
      <div class="flex items-center space-x-3">
        <mat-icon class="text-primary font-bold text-2xl" style="height: 30px; width: 30px;">health_and_safety</mat-icon>
        <span class="text-xl font-semibold text-gray-800 tracking-tight">Elderly Assistance</span>
      </div>
      
      <div class="flex items-center space-x-2">
        <button mat-icon-button color="primary" class="mr-2" routerLink="/alerts">
          <mat-icon>notifications</mat-icon>
        </button>
        <button mat-stroked-button color="warn" (click)="logout()" class="rounded-full">
          <mat-icon class="mr-1">logout</mat-icon> Logout
        </button>
      </div>
    </mat-toolbar>
  `,
  styles: [`
    .mat-mdc-toolbar { background-color: white !important; }
  `]
})
export class NavbarComponent {
  constructor(public authService: AuthService) {}

  logout() {
    this.authService.logout();
  }
}
