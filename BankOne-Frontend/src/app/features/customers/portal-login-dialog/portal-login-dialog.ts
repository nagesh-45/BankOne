import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { finalize } from 'rxjs';

import { Customer } from '../../../core/models/customer';
import { CreateUserRequest } from '../../../core/models/create-user-request';
import { Notification } from '../../../core/services/notification';
import { UserService } from '../../../core/services/user';
import { apiErrorMessage } from '../../../core/utils/api-error-message';

export type PortalLoginDialogData = {
  customer: Customer;
};

@Component({
  selector: 'app-portal-login-dialog',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule
  ],
  templateUrl: './portal-login-dialog.html',
  styleUrl: './portal-login-dialog.scss'
})
export class PortalLoginDialog {
  private readonly dialogRef = inject(MatDialogRef<PortalLoginDialog, boolean>);
  private readonly data = inject<PortalLoginDialogData>(MAT_DIALOG_DATA);
  private readonly userService = inject(UserService);
  private readonly notification = inject(Notification);

  readonly customer = this.data.customer;
  readonly saving = signal(false);

  username = this.suggestUsername();
  password = '';
  firstName = this.customer.firstName;
  lastName = this.customer.lastName;
  email = this.customer.email;

  close(): void {
    this.dialogRef.close(false);
  }

  save(): void {
    if (this.saving()) {
      return;
    }

    const request: CreateUserRequest = {
      userType: 'CUSTOMER',
      customerId: this.customer.customerId,
      username: this.username.trim(),
      password: this.password,
      firstName: this.firstName.trim(),
      lastName: this.lastName.trim(),
      email: this.email.trim()
    };

    if (
      !request.username ||
      !request.password ||
      !request.firstName ||
      !request.lastName ||
      !request.email
    ) {
      this.notification.error('Please fill in all required fields');
      return;
    }

    if (request.password.length < 6) {
      this.notification.error('Password must be at least 6 characters');
      return;
    }

    this.saving.set(true);
    this.userService.createUser(request).pipe(
      finalize(() => this.saving.set(false))
    ).subscribe({
      next: () => {
        this.notification.success(
          'Portal login created. Customer can sign in and view their own accounts.'
        );
        this.dialogRef.close(true);
      },
      error: (error) => {
        this.notification.error(
          apiErrorMessage(error, 'Failed to create portal login')
        );
      }
    });
  }

  private suggestUsername(): string {
    const emailLocal = this.customer.email?.split('@')[0]?.trim();
    if (emailLocal) {
      return emailLocal.toLowerCase().replace(/[^a-z0-9._-]/g, '');
    }
    return `c${this.customer.customerId}`;
  }
}
