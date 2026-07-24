import { Component, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  finalize,
  map,
  of,
  switchMap
} from 'rxjs';

import { Account } from '../../../core/models/account';
import { AccountService } from '../../../core/services/account';
import { Notification } from '../../../core/services/notification';
import { apiErrorMessage } from '../../../core/utils/api-error-message';

export type WithdrawDialogData = {
  account?: Account;
};

@Component({
  selector: 'app-withdraw-dialog',
  standalone: true,
  imports: [
    FormsModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule
  ],
  templateUrl: './withdraw-dialog.html',
  styleUrl: './withdraw-dialog.scss'
})
export class WithdrawDialog {
  private readonly dialogRef = inject(MatDialogRef<WithdrawDialog, boolean>);
  private readonly data = inject<WithdrawDialogData>(MAT_DIALOG_DATA, {
    optional: true
  });
  private readonly accountService = inject(AccountService);
  private readonly notification = inject(Notification);

  readonly preselectedAccount = this.data?.account ?? null;
  readonly saving = signal(false);
  readonly selectedAccount = signal<Account | null>(this.preselectedAccount);
  readonly accountSearch = signal(
    this.preselectedAccount
      ? this.displayAccount(this.preselectedAccount)
      : ''
  );

  readonly accountOptions = toSignal(
    toObservable(this.accountSearch).pipe(
      debounceTime(250),
      distinctUntilChanged(),
      switchMap((search) => {
        // If an account is already selected and the box still shows its label, keep options light
        const selected = this.selectedAccount();
        if (selected && search === this.displayAccount(selected)) {
          return of([selected]);
        }

        return this.accountService.getAccounts(search.trim(), 0, 20).pipe(
          map((page) => page.content),
          catchError(() => of([] as Account[]))
        );
      })
    ),
    { initialValue: [] as Account[] }
  );

  amount: number | null = null;

  displayAccount(account: Account | string | null): string {
    if (!account) {
      return '';
    }
    // While typing, autocomplete passes the raw string — return it as-is
    if (typeof account === 'string') {
      return account;
    }

    const customer = account.customerCode
      ? ` · ${account.customerCode}`
      : account.customerId
        ? ` · C${String(account.customerId).padStart(5, '0')}`
        : '';

    return `${account.accountNumber}${customer} · ${account.accountType} · ${account.status}`;
  }

  onAccountSearchChange(value: string): void {
    this.accountSearch.set(value);
    const selected = this.selectedAccount();
    if (selected && value !== this.displayAccount(selected)) {
      this.selectedAccount.set(null);
    }
  }

  onAccountSelected(account: Account): void {
    this.selectedAccount.set(account);
    this.accountSearch.set(this.displayAccount(account));
  }

  close(): void {
    this.dialogRef.close(false);
  }

  submit(): void {
    const amount = Number(this.amount);
    if (!Number.isFinite(amount) || amount <= 0) {
      this.notification.error('Enter a Withdraw amount greater than zero');
      return;
    }

    const account = this.selectedAccount();
    if (!account) {
      this.notification.error('Select an account from the list');
      return;
    }

    this.saving.set(true);
    this.accountService
      .withdraw(account.accountId, amount)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.notification.success('WithDraw successful');
          this.dialogRef.close(true);
        },
        error: (error) => {
          this.notification.error(apiErrorMessage(error, 'Withdraw failed'));
        }
      });
  }
}
