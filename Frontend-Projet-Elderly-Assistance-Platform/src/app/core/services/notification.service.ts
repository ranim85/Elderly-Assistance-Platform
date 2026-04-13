import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

/**
 * Central user feedback; keeps snackbar styling consistent across features.
 */
@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private snackBar = inject(MatSnackBar);

  private open(message: string, panelClass: string[], durationMs: number): void {
    /** Avoid stacked snackbars / stray CDK panes from overlapping opens. */
    this.snackBar.dismiss();
    this.snackBar.open(message, 'Close', {
      duration: durationMs,
      panelClass
    });
  }

  success(message: string, durationMs = 4000): void {
    this.open(message, ['app-snackbar-success'], durationMs);
  }

  error(message: string, durationMs = 6000): void {
    this.open(message, ['app-snackbar-error'], durationMs);
  }
}
