import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';

import { Account } from '../../../core/models/account';
import { AccountService } from '../../../core/services/account';
import { Notification } from '../../../core/services/notification';

export type AccountStatusDialogData = {
  account: Account;
};

@Component({
  selector: 'app-account-status-dialog',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule
  ],
  templateUrl: './account-status-dialog.html',
  styleUrl: './account-status-dialog.scss'
})
export class AccountStatusDialog {
  private readonly dialogRef = inject(MatDialogRef<AccountStatusDialog, boolean>);
  private readonly data = inject<AccountStatusDialogData>(MAT_DIALOG_DATA);
  private readonly accountService = inject(AccountService);
  private readonly notification = inject(Notification);

  readonly account = this.data.account;
  status = this.data.account.status;
  saving = false;

  readonly statuses = ['ACTIVE', 'DORMANT', 'SUSPENDED', 'FROZEN', 'CLOSED'];

  close(): void {
    this.dialogRef.close(false);
  }

  save(): void {
    if (!this.status) {
      this.notification.error('Please select a status');
      return;
    }

    this.saving = true;

    this.accountService.updateAccountStatus(this.account.accountId, this.status).subscribe({
      next: () => {
        this.notification.success('Account status updated');
        this.dialogRef.close(true);
      },
      error: (error) => {
        this.notification.error(
          error.error?.message ?? 'Failed to update account status'
        );
        this.saving = false;
      }
    });
  }
}
