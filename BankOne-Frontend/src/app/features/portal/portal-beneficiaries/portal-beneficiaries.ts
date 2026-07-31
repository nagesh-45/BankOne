import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { catchError, finalize, of } from 'rxjs';

import {
  Beneficiary,
  BeneficiaryBankType,
  CreateBeneficiaryRequest
} from '../../../core/models/portal-transfer';
import { Notification } from '../../../core/services/notification';
import { PortalService } from '../../../core/services/portal';
import { apiErrorMessage } from '../../../core/utils/api-error-message';
import { LoadingState } from '../../../shared/components/loading-state/loading-state';

@Component({
  selector: 'app-portal-beneficiaries',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    LoadingState
  ],
  templateUrl: './portal-beneficiaries.html',
  styleUrl: './portal-beneficiaries.scss'
})
export class PortalBeneficiaries {
  private readonly portal = inject(PortalService);
  private readonly notification = inject(Notification);

  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly list = signal<Beneficiary[]>([]);

  nickname = '';
  bankType: BeneficiaryBankType = 'SAME_BANK';
  accountNumber = '';
  accountHolderName = '';
  ifsc = '';
  bankName = '';

  constructor() {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.portal.listBeneficiaries().pipe(
      catchError(() => of([] as Beneficiary[])),
      finalize(() => this.loading.set(false))
    ).subscribe((items) => this.list.set(items));
  }

  save(): void {
    if (this.saving()) {
      return;
    }
    const request: CreateBeneficiaryRequest = {
      nickname: this.nickname.trim(),
      bankType: this.bankType,
      accountNumber: this.accountNumber.trim(),
      accountHolderName: this.accountHolderName.trim(),
      ifsc: this.bankType === 'OTHER_BANK' ? this.ifsc.trim() : undefined,
      bankName: this.bankType === 'OTHER_BANK' ? this.bankName.trim() : undefined
    };

    if (!request.nickname || !request.accountNumber || !request.accountHolderName) {
      this.notification.error('Fill nickname, account number, and holder name');
      return;
    }
    if (this.bankType === 'OTHER_BANK' && (!request.ifsc || !request.bankName)) {
      this.notification.error('IFSC and bank name are required for other-bank');
      return;
    }

    this.saving.set(true);
    this.portal.createBeneficiary(request).pipe(
      finalize(() => this.saving.set(false))
    ).subscribe({
      next: () => {
        this.notification.success('Beneficiary added');
        this.nickname = '';
        this.accountNumber = '';
        this.accountHolderName = '';
        this.ifsc = '';
        this.bankName = '';
        this.reload();
      },
      error: (error) => {
        this.notification.error(apiErrorMessage(error, 'Failed to add beneficiary'));
      }
    });
  }

  remove(id: number): void {
    this.portal.deleteBeneficiary(id).subscribe({
      next: () => {
        this.notification.success('Beneficiary removed');
        this.reload();
      },
      error: (error) => {
        this.notification.error(apiErrorMessage(error, 'Failed to remove beneficiary'));
      }
    });
  }
}
