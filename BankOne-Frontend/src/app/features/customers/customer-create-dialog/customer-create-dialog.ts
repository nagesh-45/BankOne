import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { finalize } from 'rxjs';

import { CreateCustomerRequest } from '../../../core/models/create-customer-request';
import { Customer } from '../../../core/models/customer';
import { CustomerService } from '../../../core/services/customer';
import { Notification } from '../../../core/services/notification';
import { BusinessIdPipe } from '../../../core/pipes/business-id.pipe';
import { apiErrorMessage } from '../../../core/utils/api-error-message';

export type CustomerCreateResult =
  | { action: 'view'; customerId: number }
  | { action: 'done' };

@Component({
  selector: 'app-customer-create-dialog',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    BusinessIdPipe
  ],
  templateUrl: './customer-create-dialog.html',
  styleUrl: './customer-create-dialog.scss'
})
export class CustomerCreateDialog {
  private readonly dialogRef = inject(MatDialogRef<CustomerCreateDialog, CustomerCreateResult | false>);
  private readonly customerService = inject(CustomerService);
  private readonly notification = inject(Notification);

  readonly step = signal<'form' | 'success'>('form');
  readonly createdCustomer = signal<Customer | null>(null);
  readonly saving = signal(false);
  readonly portalLoginCreated = signal(false);

  firstName = '';
  lastName = '';
  email = '';
  phoneNumber = '';
  dateOfBirth = '';
  address = '';
  status = 'ACTIVE';
  branchCode = '0001';
  accountType = 'SAVINGS';
  currencyCode = 'INR';
  openingDeposit = 1000;

  createPortalLogin = false;
  portalUsername = '';
  portalPassword = '';

  readonly statuses = ['ACTIVE', 'INACTIVE', 'SUSPENDED'];
  readonly accountTypes = ['SAVINGS', 'CURRENT', 'SALARY', 'FIXED_DEPOSIT', 'RECURRING_DEPOSIT'];
  readonly currencyCodes = ['INR'];

  onEmailChange(value: string): void {
    this.email = value;
    if (!this.createPortalLogin || this.portalUsername.trim()) {
      return;
    }
    this.portalUsername = this.suggestUsername(value);
  }

  onCreatePortalToggle(checked: boolean): void {
    this.createPortalLogin = checked;
    if (checked && !this.portalUsername.trim()) {
      this.portalUsername = this.suggestUsername(this.email);
    }
  }

  close(): void {
    if (this.step() === 'success') {
      this.dialogRef.close({ action: 'done' });
      return;
    }
    this.dialogRef.close(false);
  }

  viewCustomer(): void {
    const customer = this.createdCustomer();
    if (!customer) {
      return;
    }
    this.dialogRef.close({
      action: 'view',
      customerId: customer.customerId
    });
  }

  save(): void {
    if (this.saving()) {
      return;
    }

    const request: CreateCustomerRequest = {
      firstName: this.firstName.trim(),
      lastName: this.lastName.trim(),
      email: this.email.trim(),
      phoneNumber: this.phoneNumber.trim(),
      dateOfBirth: this.dateOfBirth || null,
      address: this.address.trim(),
      status: this.status,
      branchCode: this.branchCode.trim(),
      accountType: this.accountType,
      currencyCode: this.currencyCode,
      openingDeposit: Number(this.openingDeposit)
    };

    if (this.createPortalLogin) {
      request.portalUsername = this.portalUsername.trim();
      request.portalPassword = this.portalPassword;
    }

    if (
      !request.firstName ||
      !request.lastName ||
      !request.email ||
      !request.phoneNumber ||
      !request.address ||
      !request.status ||
      !request.branchCode ||
      !request.accountType ||
      !request.currencyCode ||
      request.openingDeposit == null ||
      Number.isNaN(request.openingDeposit)
    ) {
      this.notification.error('Please fill in all required fields');
      return;
    }

    if (!/^\d{10}$/.test(request.phoneNumber)) {
      this.notification.error('Phone number must be 10 digits');
      return;
    }

    if (!/^\d{4}$/.test(request.branchCode)) {
      this.notification.error('Branch code must be exactly 4 numeric digits');
      return;
    }

    if (request.openingDeposit < 0) {
      this.notification.error('Opening deposit cannot be negative');
      return;
    }

    if (this.createPortalLogin) {
      if (!request.portalUsername || !request.portalPassword) {
        this.notification.error('Portal username and password are required');
        return;
      }
      if (request.portalPassword.length < 6) {
        this.notification.error('Portal password must be at least 6 characters');
        return;
      }
    }

    this.saving.set(true);

    this.customerService.createCustomer(request).pipe(
      finalize(() => this.saving.set(false))
    ).subscribe({
      next: (customer) => {
        this.portalLoginCreated.set(this.createPortalLogin);
        this.notification.success(
          this.createPortalLogin
            ? 'Customer, account, and portal login created'
            : 'Customer and account created successfully'
        );
        this.createdCustomer.set(customer);
        this.step.set('success');
      },
      error: (error) => {
        this.notification.error(
          apiErrorMessage(error, 'Failed to create customer')
        );
      }
    });
  }

  private suggestUsername(email: string): string {
    const local = email.split('@')[0]?.trim() ?? '';
    return local.toLowerCase().replace(/[^a-z0-9._-]/g, '');
  }
}
