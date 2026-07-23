import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class Notification {
  private readonly snackBar = inject(MatSnackBar);

  success(message: string): void {
    this.open(message, ['success-snackbar'], 2500);
  }

  error(message: string): void {
    this.open(message, ['error-snackbar'], 3500);
  }

  info(message: string): void {
    this.open(message, [], 3000);
  }

  private open(message: string, panelClass: string[], duration: number): void {
    try {
      this.snackBar.open(message, 'Close', {
        duration,
        horizontalPosition: 'center',
        verticalPosition: 'top',
        panelClass
      });
    } catch (error) {
      console.error(message, error);
    }
  }
}
