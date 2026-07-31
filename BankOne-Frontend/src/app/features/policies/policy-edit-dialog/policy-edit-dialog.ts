import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { AccountPolicy } from '../../../core/models/account-policy';
import { AccountPolicyService } from '../../../core/services/account-policy';
import { Notification } from '../../../core/services/notification';
import { apiErrorMessage } from '../../../core/utils/api-error-message';

export interface PolicyEditDialogData {
  policy: AccountPolicy;
}

@Component({
  selector: 'app-policy-edit-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule
  ],
  templateUrl: './policy-edit-dialog.html',
  styleUrl: './policy-edit-dialog.scss'
})
export class PolicyEditDialog {
  private readonly fb = inject(FormBuilder);
  private readonly accountPolicyService = inject(AccountPolicyService);
  private readonly notification = inject(Notification);
  private readonly dialogRef = inject(MatDialogRef<PolicyEditDialog, AccountPolicy | false>);
  readonly data = inject<PolicyEditDialogData>(MAT_DIALOG_DATA);

  saving = false;

  readonly form = this.fb.nonNullable.group({
    openingDepositRequired: this.data.policy.openingDepositRequired,
    requiredOpeningDeposit: [
      this.data.policy.requiredOpeningDeposit,
      [Validators.required, Validators.min(0)]
    ],
    minimumBalance: [
      this.data.policy.minimumBalance,
      [Validators.required, Validators.min(0)]
    ],
    active: this.data.policy.active,
    effectiveFrom: [
      this.toLocalInput(this.data.policy.effectiveFrom),
      Validators.required
    ],
    effectiveTo: [this.toLocalInput(this.data.policy.effectiveTo)]
  });

  cancel(): void {
    this.dialogRef.close(false);
  }

  save(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    this.saving = true;
    this.accountPolicyService.updatePolicy(this.data.policy.policyId, {
      openingDepositRequired: value.openingDepositRequired,
      requiredOpeningDeposit: Number(value.requiredOpeningDeposit),
      minimumBalance: Number(value.minimumBalance),
      active: value.active,
      effectiveFrom: this.toApiDateTime(value.effectiveFrom),
      effectiveTo: value.effectiveTo
        ? this.toApiDateTime(value.effectiveTo)
        : null
    }).subscribe({
      next: (updated) => {
        this.saving = false;
        this.dialogRef.close(updated);
      },
      error: (error) => {
        this.saving = false;
        this.notification.error(apiErrorMessage(error, 'Failed to update policy'));
      }
    });
  }

  private toLocalInput(iso: string | null): string {
    if (!iso) {
      return '';
    }
    const date = new Date(iso);
    if (Number.isNaN(date.getTime())) {
      return '';
    }
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
  }

  /** LocalDateTime-friendly string (no Z) for Spring. */
  private toApiDateTime(localInput: string): string {
    return localInput.length === 16 ? `${localInput}:00` : localInput;
  }
}
