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

export type TransferDialogData = {
  fromAccount?: Account;
};

@Component({
  selector: 'app-transfer-dialog',
  standalone: true,
  imports: [
    FormsModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule
  ],
  templateUrl: './transfer-dialog.html',
  styleUrl: './transfer-dialog.scss'
})
export class TransferDialog {
  private readonly dialogRef = inject(MatDialogRef<TransferDialog, boolean>);
  private readonly data = inject<TransferDialogData>(MAT_DIALOG_DATA, {
    optional: true
  });
  private readonly accountService = inject(AccountService);
  private readonly notification = inject(Notification);

  readonly preselectedFrom = this.data?.fromAccount ?? null;
  readonly saving = signal(false);

  readonly selectedFrom = signal<Account | null>(this.preselectedFrom);
  readonly fromSearch = signal(
    this.preselectedFrom ? this.displayAccount(this.preselectedFrom) : ''
  );

  readonly selectedTo = signal<Account | null>(null);
  readonly toSearch = signal('');

  amount: number | null = null;

  readonly fromOptions = toSignal(
    toObservable(this.fromSearch).pipe(
      debounceTime(250),
      distinctUntilChanged(),
      switchMap((search) => {
        const selected = this.selectedFrom();
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

  readonly toOptions = toSignal(
    toObservable(this.toSearch).pipe(
      debounceTime(250),
      distinctUntilChanged(),
      switchMap((search) => {
        const selected = this.selectedTo();
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

  displayAccount(account: Account | string | null): string {
    if (!account) {
      return '';
    }
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

  onFromSearchChange(value: string): void {
    this.fromSearch.set(value);
    const selected = this.selectedFrom();
    if (selected && value !== this.displayAccount(selected)) {
      this.selectedFrom.set(null);
    }
  }

  onFromSelected(account: Account): void {
    this.selectedFrom.set(account);
    this.fromSearch.set(this.displayAccount(account));
  }

  onToSearchChange(value: string): void {
    this.toSearch.set(value);
    const selected = this.selectedTo();
    if (selected && value !== this.displayAccount(selected)) {
      this.selectedTo.set(null);
    }
  }

  onToSelected(account: Account): void {
    this.selectedTo.set(account);
    this.toSearch.set(this.displayAccount(account));
  }

  close(): void {
    this.dialogRef.close(false);
  }

  submit(): void {
    const amount = Number(this.amount);
    if (!Number.isFinite(amount) || amount <= 0) {
      this.notification.error('Enter a transfer amount greater than zero');
      return;
    }

    const from = this.selectedFrom();
    const to = this.selectedTo();
    if (!from || !to) {
      this.notification.error('Select both From and To accounts');
      return;
    }
    if (from.accountId === to.accountId) {
      this.notification.error('Cannot transfer to the same account');
      return;
    }

    this.saving.set(true);
    this.accountService
      .transfer(from.accountId, to.accountId, amount)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.notification.success('Transfer successful');
          this.dialogRef.close(true);
        },
        error: (error) => {
          this.notification.error(apiErrorMessage(error, 'Transfer failed'));
        }
      });
  }
}
