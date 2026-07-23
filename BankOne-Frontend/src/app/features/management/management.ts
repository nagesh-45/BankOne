import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

import { Auth } from '../../core/services/auth';
import {
  CustomerCreateDialog,
  CustomerCreateResult
} from '../customers/customer-create-dialog/customer-create-dialog';
import {
  EmployeeCreateResult,
  UserCreateDialog
} from '../employees/user-create-dialog/user-create-dialog';

@Component({
  selector: 'app-management',
  standalone: true,
  imports: [
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule
  ],
  templateUrl: './management.html',
  styleUrl: './management.scss'
})
export class Management {
  private readonly auth = inject(Auth);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);

  readonly canCreateCustomer = this.auth.hasAnyRole(['ADMIN', 'EMPLOYEE']);
  readonly canCreateEmployee = this.auth.hasAnyRole(['ADMIN']);
  readonly canViewEmployees = this.auth.hasAnyRole(['ADMIN']);

  openCreateCustomer(): void {
    this.dialog.open<CustomerCreateDialog, void, CustomerCreateResult | false>(
      CustomerCreateDialog,
      {
        width: '640px',
        maxWidth: '95vw',
        disableClose: true
      }
    ).afterClosed().subscribe((result) => {
      if (result && result.action === 'view') {
        this.router.navigate(['/app/customers', result.customerId]);
      }
    });
  }

  openCreateEmployee(): void {
    this.dialog.open<UserCreateDialog, void, EmployeeCreateResult | false>(
      UserCreateDialog,
      {
        width: '640px',
        maxWidth: '95vw',
        disableClose: true
      }
    ).afterClosed().subscribe((result) => {
      if (result && result.action === 'view') {
        this.router.navigate(['/app/employees'], {
          queryParams: { q: result.employeeCode }
        });
      }
    });
  }
}
