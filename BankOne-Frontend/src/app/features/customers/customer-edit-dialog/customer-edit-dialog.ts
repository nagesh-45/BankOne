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
import { finalize } from 'rxjs';

import { Customer } from '../../../core/models/customer';
import { UpdateCustomerRequest } from '../../../core/models/update-customer-request';
import { CustomerService } from '../../../core/services/customer';
import { Notification } from '../../../core/services/notification';
import { apiErrorMessage } from '../../../core/utils/api-error-message';
import { BusinessIdPipe } from '../../../core/pipes/business-id.pipe';

export type CustomerEditDialogData = {
  customer: Customer;
};

@Component({
  selector: 'app-customer-edit-dialog',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    BusinessIdPipe
  ],
  templateUrl: './customer-edit-dialog.html',
  styleUrl: './customer-edit-dialog.scss'
})
export class CustomerEditDialog {
  private readonly dialogRef = inject(MatDialogRef<CustomerEditDialog, boolean>);
  private readonly data = inject<CustomerEditDialogData>(MAT_DIALOG_DATA);
  private readonly customerService = inject(CustomerService);
  private readonly notification = inject(Notification);

  readonly customer = this.data.customer;
  readonly saving = signal(false);

  firstName = this.customer.firstName;
  lastName = this.customer.lastName;
  email = this.customer.email;
  phoneNumber = this.customer.phoneNumber;
  dateOfBirth = this.customer.dateOfBirth ?? '';
  address = this.customer.address;
  status = this.customer.status;

  readonly statuses = ['ACTIVE', 'INACTIVE', 'SUSPENDED'];

  close(): void {
    this.dialogRef.close(false);
  }

  save(): void {
    if (this.saving()) {
      return;
    }

    const request: UpdateCustomerRequest = {
      firstName: this.firstName.trim(),
      lastName: this.lastName.trim(),
      email: this.email.trim(),
      phoneNumber: this.phoneNumber.trim(),
      dateOfBirth: this.dateOfBirth || null,
      address: this.address.trim(),
      status: this.status
    };

    if (
      !request.firstName ||
      !request.lastName ||
      !request.email ||
      !request.phoneNumber ||
      !request.address ||
      !request.status
    ) {
      this.notification.error('Please fill in all required fields');
      return;
    }

    if (!/^\d{10}$/.test(request.phoneNumber)) {
      this.notification.error('Phone number must be 10 digits');
      return;
    }

    this.saving.set(true);

    this.customerService.updateCustomer(this.customer.customerId, request).pipe(
      finalize(() => this.saving.set(false))
    ).subscribe({
      next: () => {
        this.notification.success('Customer updated successfully');
        this.dialogRef.close(true);
      },
      error: (error) => {
        this.notification.error(
          apiErrorMessage(error, 'Failed to update customer')
        );
      }
    });
  }
}
