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
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { finalize } from 'rxjs';

import { AppUser } from '../../../core/models/app-user';
import { AccessLevel } from '../../../core/models/create-user-request';
import { UpdateUserRequest } from '../../../core/models/update-user-request';
import { Notification } from '../../../core/services/notification';
import { UserService } from '../../../core/services/user';
import { apiErrorMessage } from '../../../core/utils/api-error-message';
import { BusinessIdPipe } from '../../../core/pipes/business-id.pipe';

export type UserEditDialogData = {
  employee: AppUser;
};

@Component({
  selector: 'app-user-edit-dialog',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    BusinessIdPipe
  ],
  templateUrl: './user-edit-dialog.html',
  styleUrl: './user-edit-dialog.scss'
})
export class UserEditDialog {
  private readonly dialogRef = inject(MatDialogRef<UserEditDialog, boolean>);
  private readonly data = inject<UserEditDialogData>(MAT_DIALOG_DATA);
  private readonly userService = inject(UserService);
  private readonly notification = inject(Notification);

  readonly employee = this.data.employee;
  readonly saving = signal(false);

  firstName = this.employee.firstName;
  lastName = this.employee.lastName;
  email = this.employee.email;
  enabled = this.employee.enabled;
  accessLevel: AccessLevel = this.employee.roles.includes('ADMIN') ? 'ADMIN' : 'NORMAL';

  close(): void {
    this.dialogRef.close(false);
  }

  save(): void {
    if (this.saving()) {
      return;
    }

    const request: UpdateUserRequest = {
      firstName: this.firstName.trim(),
      lastName: this.lastName.trim(),
      email: this.email.trim(),
      enabled: this.enabled,
      accessLevel: this.accessLevel
    };

    if (!request.firstName || !request.lastName || !request.email) {
      this.notification.error('Please fill in all required fields');
      return;
    }

    this.saving.set(true);

    this.userService.updateUser(this.employee.userId, request).pipe(
      finalize(() => this.saving.set(false))
    ).subscribe({
      next: () => {
        this.notification.success('Employee updated successfully');
        this.dialogRef.close(true);
      },
      error: (error) => {
        this.notification.error(
          apiErrorMessage(error, 'Failed to update employee')
        );
      }
    });
  }
}
