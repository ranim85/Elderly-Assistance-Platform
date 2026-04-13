import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { MedicationDTO, MedicationService } from './medication.service';
import { AuthService } from '../../core/services/auth.service';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { NotificationService } from '../../core/services/notification.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-medication-hub',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, MatCardModule, MatIconModule, MatButtonModule, MatInputModule, MatFormFieldModule],
  template: `
    <div class="p-6 max-w-5xl mx-auto">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between mb-8 gap-4">
        <div>
          <a routerLink="/elderly-persons" class="text-blue-500 hover:underline flex items-center gap-1 mb-2">
            <mat-icon inline>arrow_back</mat-icon> Back to List
          </a>
          <h1 class="text-3xl font-bold text-gray-800">
            Medication Hub
          </h1>
          <p class="text-gray-500 mt-1">Manage and track prescriptions for Patient ID: {{ elderlyId }}</p>
        </div>
        <button *ngIf="canAdd" (click)="toggleNewMedForm()" class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg font-medium shadow transition-colors flex items-center">
          <mat-icon class="mr-2">{{ showNewMedForm ? 'close' : 'add' }}</mat-icon> 
          {{ showNewMedForm ? 'Cancel Form' : 'Add Medication' }}
        </button>
      </div>

      <!-- Add New Medication Form -->
      <mat-card *ngIf="showNewMedForm" class="mb-8 p-6 shadow-md border border-t-4 border-t-blue-500">
        <h2 class="text-xl font-bold text-gray-700 mb-4">Prescribe new medication</h2>
        <form (ngSubmit)="submitMedication()" #medForm="ngForm" class="grid grid-cols-1 sm:grid-cols-3 gap-6 items-start">
          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Medication Name</mat-label>
            <input matInput [(ngModel)]="newMed.name" name="name" required placeholder="e.g. Aspirin">
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Dosage</mat-label>
            <input matInput [(ngModel)]="newMed.dosage" name="dosage" required placeholder="e.g. 50mg">
          </mat-form-field>

          <mat-form-field appearance="outline" class="w-full">
            <mat-label>Scheduled Time</mat-label>
            <input matInput type="datetime-local" [(ngModel)]="newMed.scheduledTime" name="scheduledTime" required>
          </mat-form-field>

          <div class="sm:col-span-3 flex justify-end">
             <button mat-raised-button color="primary" type="submit" [disabled]="!medForm.valid">
               Save Prescription
             </button>
          </div>
        </form>
      </mat-card>

      <!-- Medication Schedule -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div *ngIf="isLoading" class="col-span-full py-12 text-center text-gray-500">Loading schedule...</div>
          <div *ngIf="!isLoading && medications.length === 0" class="col-span-full bg-white p-12 text-center text-gray-500 rounded-xl border border-gray-200 shadow-sm">
             <mat-icon class="text-gray-300 block mx-auto scale-150 mb-4">medical_information</mat-icon>
             <p class="text-lg">No active medications scheduled.</p>
          </div>

          <mat-card *ngFor="let med of medications" class="overflow-hidden transition-all hover:shadow-md border" [ngClass]="med.isTaken ? 'border-green-200 bg-green-50/50' : 'border-gray-200 bg-white'">
             <div class="h-2 w-full" [ngClass]="med.isTaken ? 'bg-green-500' : 'bg-orange-400'"></div>
             <mat-card-header class="mt-4">
                <mat-card-title class="flex items-center gap-2">
                    <mat-icon [ngClass]="med.isTaken ? 'text-green-600' : 'text-gray-400'">
                        {{ med.isTaken ? 'check_circle' : 'medication' }}
                    </mat-icon>
                    <span class="font-bold text-gray-800">{{ med.name }}</span>
                </mat-card-title>
                <mat-card-subtitle class="text-gray-500 ml-8 mt-1">{{ med.dosage }}</mat-card-subtitle>
             </mat-card-header>

             <mat-card-content class="mt-4 pb-2">
                 <div class="bg-gray-50 px-3 py-2 rounded border border-gray-100 flex justify-between items-center text-sm mb-3">
                    <span class="text-gray-500 font-medium tracking-wide text-xs uppercase">Scheduled for</span>
                    <span class="font-semibold text-gray-800">{{ med.scheduledTime | date:'shortTime' }}</span>
                 </div>
                 <div class="mt-2 text-sm text-center bg-white p-2 rounded border border-dashed border-gray-200">
                    <span *ngIf="med.isTaken" class="text-green-600 font-bold">● Taken at {{ med.timeTaken | date:'shortTime' }}</span>
                    <span *ngIf="!med.isTaken" class="text-orange-500 font-bold tracking-wide text-xs uppercase">● Pending log</span>
                 </div>
             </mat-card-content>
             <mat-card-actions class="px-4 pb-4 mt-auto" *ngIf="!med.isTaken && (authService.userRole$ | async) === 'CAREGIVER'">
                <button mat-flat-button class="w-full !bg-green-600 !text-white hover:!bg-green-700" (click)="markTaken(med.id)">
                   Log Intake
                </button>
             </mat-card-actions>
          </mat-card>
      </div>

    </div>
  `
})
export class MedicationHubComponent implements OnInit {
   private route = inject(ActivatedRoute);
   public authService = inject(AuthService);
   private medicationService = inject(MedicationService);
   private notifications = inject(NotificationService);

   elderlyId!: number;
   medications: MedicationDTO[] = [];
   canAdd = false;
   showNewMedForm = false;
   isLoading = true;

   newMed = {
       name: '',
       dosage: '',
       scheduledTime: ''
   };

   ngOnInit() {
       this.elderlyId = Number(this.route.snapshot.paramMap.get('elderlyId'));
       const role = this.authService.getRole();
       this.canAdd = role === 'CAREGIVER' || role === 'ADMIN';
       this.loadMedications();
   }

   loadMedications() {
       this.isLoading = true;
       this.medicationService.getMedicationsByElderly(this.elderlyId).subscribe({
           next: meds => {
               this.medications = meds.sort((a, b) => {
                   if (a.isTaken === b.isTaken) return new Date(a.scheduledTime).getTime() - new Date(b.scheduledTime).getTime();
                   return a.isTaken ? 1 : -1;
               });
               this.isLoading = false;
           },
           error: err => {
               console.error(err);
               this.notifications.error('Failed to load medications schedule.');
               this.isLoading = false;
           }
       });
   }

   toggleNewMedForm() {
       this.showNewMedForm = !this.showNewMedForm;
       if (!this.showNewMedForm) this.resetForm();
   }

   resetForm() {
       this.newMed = { name: '', dosage: '', scheduledTime: '' };
   }

   submitMedication() {
       const payload = {
           elderlyPersonId: this.elderlyId,
           name: this.newMed.name,
           dosage: this.newMed.dosage,
           scheduledTime: this.newMed.scheduledTime
       };

       this.medicationService.createMedication(payload).subscribe({
           next: () => {
               this.notifications.success('New medication added to schedule.');
               this.toggleNewMedForm();
               this.loadMedications();
           },
           error: (err: HttpErrorResponse) => {
               this.notifications.error(err.error?.message || 'Failed to add medication');
           }
       });
   }

   markTaken(id: number) {
       this.medicationService.markAsTaken(id).subscribe({
           next: () => {
               this.notifications.success('Medication intake logged.');
               this.loadMedications();
           },
           error: (err: HttpErrorResponse) => {
               this.notifications.error('Failed to log intake');
           }
       });
   }
}
