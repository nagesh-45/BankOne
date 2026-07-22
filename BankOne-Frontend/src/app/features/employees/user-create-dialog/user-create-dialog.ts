import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';

import {
  AccessLevel,
  CreateUserRequest,
  UserType
} from '../../../core/models/create-user-request';
import { Notification } from '../../../core/services/notification';
import { UserService } from '../../../core/services/user';

@Component({
  selector: 'app-user-create-dialog',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatRadioModule,
    MatSelectModule
  ],
  templateUrl: './user-create-dialog.html',
  styleUrl: './user-create-dialog.scss'
})
export class UserCreateDialog {
  private readonly dialogRef = inject(MatDialogRef<UserCreateDialog, boolean>);
  private readonly userService = inject(UserService);
  private readonly notification = inject(Notification);

  step: 'type' | 'employee' = 'type';
  userType: UserType = 'EMPLOYEE';
  accessLevel: AccessLevel = 'NORMAL';

  username = '';
  password = '';
  firstName = '';
  lastName = '';
  email = '';
  saving = false;

  close(): void {
    this.dialogRef.close(false);
  }

  continueToForm(): void {
    if (this.userType === 'CUSTOMER') {
      this.notification.info(
        'Bank customer login creation is coming next. Create an employee for now.'
      );
      return;
    }

    this.step = 'employee';
  }

  backToType(): void {
    this.step = 'type';
  }

  save(): void {
    const request: CreateUserRequest = {
      userType: 'EMPLOYEE',
      accessLevel: this.accessLevel,
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

    this.saving = true;

    this.userService.createUser(request).subscribe({
      next: () => {
        this.notification.success('Employee created successfully');
        this.dialogRef.close(true);
      },
      error: (error) => {
        this.notification.error(
          error.error?.message ?? 'Failed to create employee'
        );
        this.saving = false;
      }
    });
  }
}
