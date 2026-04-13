import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LocationService, ElderlySettingsDTO } from './location.service';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { NotificationService } from '../../core/services/notification.service';
import { ActivatedRoute, RouterModule } from '@angular/router';

@Component({
  selector: 'app-geofence-simulator',
  standalone: true,
  imports: [CommonModule, RouterModule, MatCardModule, MatButtonModule, MatInputModule, MatFormFieldModule, MatIconModule, FormsModule],
  template: `
    <div class="p-6 max-w-4xl mx-auto">
      <div class="mb-8">
        <a routerLink="/elderly-persons" class="text-blue-500 hover:underline flex items-center gap-1 mb-2">
            <mat-icon inline>arrow_back</mat-icon> Back to List
        </a>
        <h1 class="text-3xl font-bold text-gray-800 flex items-center gap-3">
          <mat-icon class="text-indigo-500 scale-150">satellite_alt</mat-icon> Geofencing Tracker
        </h1>
        <p class="text-gray-500 mt-2">Set home boundaries and simulate real-time smartwatch tracking for Elderly ID {{ elderlyId }}</p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
        <!-- Settings Form -->
        <mat-card class="p-6 shadow-sm border border-gray-100">
          <h2 class="text-xl font-bold text-gray-700 mb-4 flex items-center gap-2">
            <mat-icon class="text-blue-500">home</mat-icon> Home Settings
          </h2>
          <form (ngSubmit)="saveSettings()" class="space-y-4">
            <mat-form-field appearance="outline" class="w-full">
              <mat-label>Home Latitude</mat-label>
              <input matInput type="number" [(ngModel)]="settings.homeLatitude" name="homeLatitude" required>
            </mat-form-field>
            <mat-form-field appearance="outline" class="w-full">
              <mat-label>Home Longitude</mat-label>
              <input matInput type="number" [(ngModel)]="settings.homeLongitude" name="homeLongitude" required>
            </mat-form-field>
            <mat-form-field appearance="outline" class="w-full">
              <mat-label>Safe Zone Radius (Meters)</mat-label>
              <input matInput type="number" [(ngModel)]="settings.safeZoneRadius" name="safeZoneRadius" required>
            </mat-form-field>
            
            <button mat-raised-button color="primary" type="submit" class="w-full text-base py-2">Save Calibration</button>
          </form>
        </mat-card>

        <!-- Simulator Form -->
        <mat-card class="p-6 shadow-md border-2 border-dashed border-indigo-200 bg-indigo-50/30 relative overflow-hidden">
          <div class="absolute -right-4 -top-4 opacity-5 text-indigo-900 pointer-events-none">
             <mat-icon [inline]="true" style="font-size: 10rem;">watch</mat-icon>
          </div>
          <h2 class="text-xl font-bold text-indigo-800 mb-4 flex items-center gap-2 relative z-10">
            <mat-icon>smart_toy</mat-icon> Smartwatch Simulator
          </h2>
          <p class="text-sm text-gray-600 mb-6 relative z-10">Fire a simulated GPS ping. If the distance exceeds the Safe Zone Radius, a WANDERING_EMERGENCY websocket alert is instantly emitted.</p>
          <div class="space-y-4 relative z-10">
            <mat-form-field appearance="outline" class="w-full bg-white">
              <mat-label>Current Latitude</mat-label>
              <input matInput type="number" [(ngModel)]="pingLat" name="pingLat">
            </mat-form-field>
            <mat-form-field appearance="outline" class="w-full bg-white">
              <mat-label>Current Longitude</mat-label>
              <input matInput type="number" [(ngModel)]="pingLng" name="pingLng">
            </mat-form-field>
            
            <div class="flex gap-4">
                <button mat-stroked-button (click)="loadCurrentLocation()" class="flex-1 bg-white border-indigo-300 text-indigo-700">
                   <mat-icon>my_location</mat-icon> Sensor
                </button>
                <button mat-flat-button color="accent" (click)="sendPing()" class="flex-1 text-base">
                   <mat-icon>cell_tower</mat-icon> Emit Ping
                </button>
            </div>
          </div>
        </mat-card>
      </div>
    </div>
  `
})
export class GeofenceSimulatorComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private locationService = inject(LocationService);
  private notifications = inject(NotificationService);

  elderlyId!: number;
  settings: ElderlySettingsDTO = { elderlyId: 0, homeLatitude: 0, homeLongitude: 0, safeZoneRadius: 50 };
  
  pingLat = 0;
  pingLng = 0;

  ngOnInit() {
    this.elderlyId = Number(this.route.snapshot.paramMap.get('elderlyId'));
    this.locationService.getSettings(this.elderlyId).subscribe({
      next: res => {
          this.settings = res;
          this.pingLat = res.homeLatitude || 0;
          this.pingLng = res.homeLongitude || 0;
      },
      error: () => this.notifications.error('Could not load geofence settings')
    });
  }

  saveSettings() {
    this.settings.elderlyId = this.elderlyId;
    this.locationService.updateSettings(this.settings).subscribe({
        next: () => this.notifications.success('Home coordinates calibrated successfully.'),
        error: () => this.notifications.error('Failed to update geofence.')
    });
  }

  loadCurrentLocation() {
      if(navigator.geolocation) {
          navigator.geolocation.getCurrentPosition((pos) => {
              this.pingLat = pos.coords.latitude;
              this.pingLng = pos.coords.longitude;
              this.notifications.success('GPS injected from browser.');
          }, () => this.notifications.error('Geolocation denied or failed.'));
      }
  }

  sendPing() {
      this.locationService.pingLocation({
          elderlyId: this.elderlyId,
          latitude: this.pingLat,
          longitude: this.pingLng
      }).subscribe({
          next: () => this.notifications.success('GPS Ping sent to backend server.'),
          error: () => this.notifications.error('Network error during ping.')
      });
  }
}
