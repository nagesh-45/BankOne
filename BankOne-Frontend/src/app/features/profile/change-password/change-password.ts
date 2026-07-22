import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { finalize } from 'rxjs';

import { Auth } from '../../../core/services/auth';
import { Notification } from '../../../core/services/notification';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule
  ],
  templateUrl: './change-password.html',
  styleUrl: './change-password.scss'
})
export class ChangePassword {
  private readonly auth = inject(Auth);
  private readonly notification = inject(Notification);
  private readonly router = inject(Router);

  currentPassword = '';
  newPassword = '';
  confirmPassword = '';
  hideCurrent = true;
  hideNew = true;
  hideConfirm = true;
  readonly saving = signal(false);

  submit(): void {
    const currentPassword = this.currentPassword;
    const newPassword = this.newPassword;
    const confirmPassword = this.confirmPassword;

    if (!currentPassword || !newPassword || !confirmPassword) {
      this.notification.error('All password fields are required');
      return;
    }

    if (newPassword.length < 8) {
      this.notification.error('New password must be at least 8 characters');
      return;
    }

    if (newPassword !== confirmPassword) {
      this.notification.error('New password and confirm password do not match');
      return;
    }

    this.saving.set(true);

    this.auth.changePassword({
      currentPassword,
      newPassword,
      confirmPassword
    }).pipe(
      finalize(() => this.saving.set(false))
    ).subscribe({
      next: () => {
        this.notification.success('Password updated successfully');
        this.router.navigate(['/app/profile']);
      },
      error: (error) => {
        this.notification.error(
          error.error?.message ?? 'Failed to update password'
        );
      }
    });
  }
}
