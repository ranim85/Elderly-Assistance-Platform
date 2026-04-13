import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TimelineEventDTO, TimelineService } from './timeline.service';
import { Observable } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';

@Component({
  selector: 'app-family-timeline',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatChipsModule],
  template: `
    <div class="px-4 py-8 max-w-4xl mx-auto">
      <h2 class="text-3xl font-bold text-gray-800 mb-8 flex items-center gap-3">
        <mat-icon class="text-blue-500 scale-150">schedule</mat-icon> Timeline Familiale
      </h2>
      
      <div *ngIf="timeline$ | async as events; else loading">
        <div *ngIf="events.length === 0" class="text-center text-gray-500 bg-white p-8 rounded-lg shadow-sm">
          Aucun événement à afficher pour le moment.
        </div>
        
        <div *ngIf="events.length > 0" class="relative border-l-4 border-gray-100 ml-4 pl-8 py-2 space-y-8">
          <div *ngFor="let event of events" class="relative">
            <!-- Icon Point on the line -->
            <div class="absolute -left-[42px] top-4 bg-white rounded-full p-1 border-2" [ngClass]="getBorderColor(event.severity)">
              <mat-icon class="block" [ngClass]="getTextColor(event.severity)">{{ getIcon(event.eventType) }}</mat-icon>
            </div>
            
            <!-- Event Card -->
            <mat-card class="shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
              <mat-card-header>
                <div mat-card-avatar class="hidden sm:block mt-2">
                    <mat-icon [ngClass]="getTextColor(event.severity)">{{ getIcon(event.eventType) }}</mat-icon>
                </div>
                <mat-card-title class="text-lg font-semibold text-gray-800">{{ event.title }}</mat-card-title>
                <mat-card-subtitle class="text-gray-500">{{ event.date | date:'dd/MM/yyyy HH:mm' }}</mat-card-subtitle>
              </mat-card-header>
              <mat-card-content class="mt-4">
                <p class="text-gray-700 whitespace-pre-line">{{ event.description }}</p>
              </mat-card-content>
              <mat-card-actions class="px-4 pb-4">
                <mat-chip [ngClass]="getChipClass(event.severity)">{{ event.severity }}</mat-chip>
              </mat-card-actions>
            </mat-card>
          </div>
        </div>
      </div>
      
      <ng-template #loading>
        <div class="animate-pulse space-y-8 ml-4 pl-8 border-l-4 border-gray-100 relative">
          <div class="h-32 bg-gray-200 rounded-lg w-full"></div>
          <div class="h-32 bg-gray-200 rounded-lg w-full"></div>
        </div>
      </ng-template>
    </div>
  `
})
export class FamilyTimelineComponent implements OnInit {
  private timelineService = inject(TimelineService);
  timeline$!: Observable<TimelineEventDTO[]>;

  ngOnInit() {
    this.timeline$ = this.timelineService.getFamilyTimeline();
  }

  getIcon(eventType: string): string {
    switch(eventType) {
      case 'ALERT': return 'warning';
      case 'APPOINTMENT': return 'calendar_today';
      case 'HEALTH_RECORD': return 'favorite';
      case 'MEDICATION': return 'medical_services';
      default: return 'info';
    }
  }

  getTextColor(severity: string): string {
    switch(severity) {
      case 'CRITICAL': return 'text-red-600';
      case 'WARNING': return 'text-orange-500';
      case 'SUCCESS': return 'text-green-500';
      case 'INFO': return 'text-blue-500';
      default: return 'text-gray-500';
    }
  }

  getBorderColor(severity: string): string {
    switch(severity) {
      case 'CRITICAL': return 'border-red-600';
      case 'WARNING': return 'border-orange-500';
      case 'SUCCESS': return 'border-green-500';
      case 'INFO': return 'border-blue-500';
      default: return 'border-gray-500';
    }
  }

  getChipClass(severity: string): string {
    switch(severity) {
      case 'CRITICAL': return '!bg-red-100 !text-red-800 font-bold';
      case 'WARNING': return '!bg-orange-100 !text-orange-800 font-bold';
      case 'SUCCESS': return '!bg-green-100 !text-green-800 font-bold';
      case 'INFO': return '!bg-blue-100 !text-blue-800 font-bold';
      default: return '!bg-gray-100 !text-gray-800';
    }
  }
}
