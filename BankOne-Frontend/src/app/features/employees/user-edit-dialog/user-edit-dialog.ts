import { Component, OnInit, inject, signal } from '@angular/core';
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
import { RoleSummary } from '../../../core/models/role';
import { UpdateUserRequest } from '../../../core/models/update-user-request';
import { Notification } from '../../../core/services/notification';
import { RoleService } from '../../../core/services/role';
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
export class UserEditDialog implements OnInit {
  private readonly dialogRef = inject(MatDialogRef<UserEditDialog, boolean>);
  private readonly data = inject<UserEditDialogData>(MAT_DIALOG_DATA);
  private readonly userService = inject(UserService);
  private readonly roleService = inject(RoleService);
  private readonly notification = inject(Notification);

  readonly employee = this.data.employee;
  readonly saving = signal(false);
  readonly staffRoles = signal<RoleSummary[]>([]);

  firstName = this.employee.firstName;
  lastName = this.employee.lastName;
  email = this.employee.email;
  enabled = this.employee.enabled;
  roleNames: string[] = [...(this.employee.roles ?? [])].filter(
    (role) => role !== 'CUSTOMER'
  );

  ngOnInit(): void {
    if (this.roleNames.length === 0) {
      this.roleNames = ['EMPLOYEE'];
    }

    this.roleService.listRoles().subscribe({
      next: (roles) => {
        this.staffRoles.set(roles.filter((role) => role.roleName !== 'CUSTOMER'));
      },
      error: () => {
        this.staffRoles.set([
          { roleId: 0, roleName: 'ADMIN', description: null, accessCodes: [], systemRole: true },
          { roleId: 0, roleName: 'EMPLOYEE', description: null, accessCodes: [], systemRole: true },
          { roleId: 0, roleName: 'MANAGER', description: null, accessCodes: [], systemRole: true },
          { roleId: 0, roleName: 'TELLER', description: null, accessCodes: [], systemRole: true },
          { roleId: 0, roleName: 'AUDITOR', description: null, accessCodes: [], systemRole: true }
        ]);
      }
    });
  }

  hasRole(roleName: string): boolean {
    return this.roleNames.includes(roleName);
  }

  toggleRole(roleName: string, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    if (checked) {
      if (!this.roleNames.includes(roleName)) {
        this.roleNames = [...this.roleNames, roleName];
      }
      return;
    }

    if (this.roleNames.length <= 1) {
      this.notification.error('Keep at least one role');
      (event.target as HTMLInputElement).checked = true;
      return;
    }

    this.roleNames = this.roleNames.filter((name) => name !== roleName);
  }

  close(): void {
    this.dialogRef.close(false);
  }

  save(): void {
    if (this.saving()) {
      return;
    }

    const selectedRoles = [...new Set(this.roleNames.filter(Boolean))];
    const request: UpdateUserRequest = {
      firstName: this.firstName.trim(),
      lastName: this.lastName.trim(),
      email: this.email.trim(),
      enabled: this.enabled,
      roleNames: selectedRoles
    };

    if (!request.firstName || !request.lastName || !request.email || selectedRoles.length === 0) {
      this.notification.error('Please fill in all required fields and select at least one role');
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
