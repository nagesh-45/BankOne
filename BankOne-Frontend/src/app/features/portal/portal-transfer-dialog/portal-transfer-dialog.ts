import { CurrencyPipe } from '@angular/common';
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
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { finalize } from 'rxjs';

import { Account } from '../../../core/models/account';
import { Beneficiary } from '../../../core/models/portal-transfer';
import { Notification } from '../../../core/services/notification';
import { PortalService } from '../../../core/services/portal';
import { apiErrorMessage } from '../../../core/utils/api-error-message';

export type PortalTransferDialogData = {
  accounts: Account[];
  fromAccountId?: number;
};

@Component({
  selector: 'app-portal-transfer-dialog',
  standalone: true,
  imports: [
    CurrencyPipe,
    FormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatRadioModule,
    MatSelectModule
  ],
  templateUrl: './portal-transfer-dialog.html',
  styleUrl: './portal-transfer-dialog.scss'
})
export class PortalTransferDialog implements OnInit {
  private readonly dialogRef = inject(MatDialogRef<PortalTransferDialog, boolean>);
  private readonly data = inject<PortalTransferDialogData>(MAT_DIALOG_DATA);
  private readonly portal = inject(PortalService);
  private readonly notification = inject(Notification);

  readonly accounts = this.data.accounts;
  readonly saving = signal(false);
  readonly beneficiaries = signal<Beneficiary[]>([]);

  mode: 'quick' | 'beneficiary' = 'quick';
  fromAccountId = this.data.fromAccountId ?? this.accounts[0]?.accountId ?? 0;
  toAccountNumber = '';
  beneficiaryId: number | null = null;
  amount: number | null = null;
  narration = '';

  ngOnInit(): void {
    this.portal.listBeneficiaries().subscribe({
      next: (list) => this.beneficiaries.set(list),
      error: () => this.beneficiaries.set([])
    });
  }

  close(): void {
    this.dialogRef.close(false);
  }

  save(): void {
    if (this.saving() || !this.fromAccountId || !this.amount || this.amount <= 0) {
      this.notification.error('Select account and enter a valid amount');
      return;
    }

    const request =
      this.mode === 'quick'
        ? {
            amount: Number(this.amount),
            toAccountNumber: this.toAccountNumber.trim(),
            narration: this.narration.trim() || undefined
          }
        : {
            amount: Number(this.amount),
            beneficiaryId: this.beneficiaryId ?? undefined,
            narration: this.narration.trim() || undefined
          };

    if (this.mode === 'quick' && !request.toAccountNumber) {
      this.notification.error('Enter destination BankOne account number');
      return;
    }
    if (this.mode === 'beneficiary' && !request.beneficiaryId) {
      this.notification.error('Select a beneficiary');
      return;
    }

    this.saving.set(true);
    this.portal.transfer(this.fromAccountId, request).pipe(
      finalize(() => this.saving.set(false))
    ).subscribe({
      next: (result) => {
        if (result.outcome === 'PENDING_APPROVAL') {
          this.notification.info(result.message || 'Sent for employee approval');
        } else {
          this.notification.success('Transfer completed');
        }
        this.dialogRef.close(true);
      },
      error: (error) => {
        this.notification.error(apiErrorMessage(error, 'Transfer failed'));
      }
    });
  }
}
