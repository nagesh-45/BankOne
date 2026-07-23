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

import { Notification } from '../../core/services/notification';

export type OpeningDepositDialogData = {
  accountType: string;
  currencyCode: string;
  requiredOpeningDeposit: number;
};

/** Closes with the amount (number), or false if cancelled. */
@Component({
  selector: 'app-opening-deposit-dialog',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule
  ],
  templateUrl: './opening-deposit-dialog.html',
  styleUrl: './opening-deposit-dialog.scss'
})
export class OpeningDepositDialog {
  private readonly dialogRef = inject(
    MatDialogRef<OpeningDepositDialog, number | false>
  );
  private readonly data = inject<OpeningDepositDialogData>(MAT_DIALOG_DATA);
  private readonly notification = inject(Notification);

  readonly accountType = this.data.accountType;
  readonly currencyCode = this.data.currencyCode;
  readonly requiredOpeningDeposit = Number(this.data.requiredOpeningDeposit) || 0;

  amount: number | null = this.requiredOpeningDeposit || null;

  close(): void {
    this.dialogRef.close(false);
  }

  confirm(): void {
    const amount = Number(this.amount);
    if (!Number.isFinite(amount) || amount < 0) {
      this.notification.error('Enter a valid opening deposit');
      return;
    }
    if (amount < this.requiredOpeningDeposit) {
      this.notification.error(
        `Minimum opening deposit is ${this.requiredOpeningDeposit} ${this.currencyCode}`
      );
      return;
    }
    this.dialogRef.close(amount);
  }
}
