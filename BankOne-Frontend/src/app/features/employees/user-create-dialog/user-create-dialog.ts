import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { finalize } from 'rxjs';

import {
  CreateUserRequest,
  UserType
} from '../../../core/models/create-user-request';
import { RoleSummary } from '../../../core/models/role';
import { AppUser } from '../../../core/models/app-user';
import { Notification } from '../../../core/services/notification';
import { RoleService } from '../../../core/services/role';
import { UserService } from '../../../core/services/user';
import { BusinessIdPipe } from '../../../core/pipes/business-id.pipe';
import { apiErrorMessage } from '../../../core/utils/api-error-message';

export type EmployeeCreateResult =
  | { action: 'view'; employeeCode: string }
  | { action: 'done' };

@Component({
  selector: 'app-user-create-dialog',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatRadioModule,
    MatSelectModule,
    BusinessIdPipe
  ],
  templateUrl: './user-create-dialog.html',
  styleUrl: './user-create-dialog.scss'
})
export class UserCreateDialog implements OnInit {
  private readonly dialogRef = inject(MatDialogRef<UserCreateDialog, EmployeeCreateResult | false>);
  private readonly userService = inject(UserService);
  private readonly roleService = inject(RoleService);
  private readonly notification = inject(Notification);

  readonly step = signal<'type' | 'employee' | 'success'>('type');
  readonly createdEmployee = signal<AppUser | null>(null);
  readonly saving = signal(false);
  readonly staffRoles = signal<RoleSummary[]>([]);

  userType: UserType = 'EMPLOYEE';
  roleNames: string[] = ['EMPLOYEE'];

  username = '';
  password = '';
  firstName = '';
  lastName = '';
  email = '';

  ngOnInit(): void {
    this.roleService.listRoles().subscribe({
      next: (roles) => {
        const staff = roles.filter((role) => role.roleName !== 'CUSTOMER');
        this.staffRoles.set(staff);
        this.roleNames = this.roleNames.filter((name) =>
          staff.some((role) => role.roleName === name)
        );
        if (this.roleNames.length === 0 && staff[0]) {
          this.roleNames = [staff[0].roleName];
        }
      },
      error: () => {
        this.staffRoles.set([
          { roleId: 0, roleName: 'ADMIN', description: null, accessCodes: [], systemRole: true },
          { roleId: 0, roleName: 'EMPLOYEE', description: null, accessCodes: [], systemRole: true },
          { roleId: 0, roleName: 'MANAGER', description: null, accessCodes: [], systemRole: true }
        ]);
      }
    });
  }

  close(): void {
    if (this.step() === 'success') {
      this.dialogRef.close({ action: 'done' });
      return;
    }
    this.dialogRef.close(false);
  }

  continueToForm(): void {
    if (this.userType === 'CUSTOMER') {
      this.notification.info(
        'Bank customer login creation is coming next. Create an employee for now.'
      );
      return;
    }

    this.step.set('employee');
  }

  backToType(): void {
    if (this.saving()) {
      return;
    }
    this.step.set('type');
  }

  viewEmployee(): void {
    const employee = this.createdEmployee();
    if (!employee) {
      return;
    }
    const code = employee.employeeCode
      ?? `E${String(employee.userId).padStart(5, '0')}`;
    this.dialogRef.close({
      action: 'view',
      employeeCode: code
    });
  }

  save(): void {
    if (this.saving()) {
      return;
    }

    const selectedRoles = [...new Set(this.roleNames.filter(Boolean))];
    const request: CreateUserRequest = {
      userType: 'EMPLOYEE',
      roleNames: selectedRoles,
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
      !request.email ||
      selectedRoles.length === 0
    ) {
      this.notification.error('Please fill in all required fields and select at least one role');
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
      next: (employee) => {
        this.notification.success('Employee created successfully');
        this.createdEmployee.set(employee);
        this.step.set('success');
      },
      error: (error) => {
        this.notification.error(
          apiErrorMessage(error, 'Failed to create employee')
        );
      }
    });
  }
}
