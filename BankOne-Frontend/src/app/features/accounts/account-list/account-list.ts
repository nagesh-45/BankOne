import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import {
  catchError,
  combineLatest,
  debounceTime,
  distinctUntilChanged,
  map,
  of,
  startWith,
  switchMap
} from 'rxjs';
import { TransferDialog } from '../transfer-dialog/transfer-dialog';
import { Account } from '../../../core/models/account';
import { PagedResponse } from '../../../core/models/paged-response';
import { AccountService } from '../../../core/services/account';
import { ListPagination } from '../../../shared/components/list-pagination/list-pagination';
import { LoadingState } from '../../../shared/components/loading-state/loading-state';
import { BusinessIdPipe } from '../../../core/pipes/business-id.pipe';
import { DepositDialog } from '../deposit-dialog/deposit-dialog';
import { WithdrawDialog } from '../withdraw-dialog/withdraw-dialog';

@Component({
  selector: 'app-account-list',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    ListPagination,
    LoadingState,
    BusinessIdPipe
  ],
  templateUrl: './account-list.html',
  styleUrl: './account-list.scss'
})
export class AccountList {
  private readonly accountService = inject(AccountService);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);

  readonly searchTerm = signal('');
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);
  readonly sortBy = signal('createdAt');
  readonly sortDir = signal<'asc' | 'desc'>('desc');
  private readonly reloadTick = signal(0);

  private readonly accountResponse = toSignal(
    combineLatest([
      toObservable(this.searchTerm).pipe(
        debounceTime(250),
        distinctUntilChanged()
      ),
      toObservable(this.pageIndex),
      toObservable(this.pageSize),
      toObservable(this.sortBy),
      toObservable(this.sortDir),
      toObservable(this.reloadTick)
    ]).pipe(
      switchMap(([search, page, size, sortBy, sortDir]) =>
        this.accountService.getAccounts(search.trim(), page, size, sortBy, sortDir).pipe(
          map((response) => ({
            state: 'loaded' as const,
            response
          })),
          startWith({
            state: 'loading' as const,
            response: null as PagedResponse<Account> | null
          }),
          catchError((error) => {
            console.error('Failed to load accounts', error);
            return of({
              state: 'error' as const,
              response: null as PagedResponse<Account> | null
            });
          })
        )
      )
    ),
    {
      initialValue: {
        state: 'loading' as const,
        response: null as PagedResponse<Account> | null
      }
    }
  );

  readonly accounts = computed(() => this.accountResponse().response?.content ?? []);
  readonly totalAccounts = computed(
    () => this.accountResponse().response?.totalElements ?? 0
  );
  readonly totalPages = computed(
    () => this.accountResponse().response?.totalPages ?? 0
  );
  readonly isLoading = computed(() => this.accountResponse().state === 'loading');
  readonly hasError = computed(() => this.accountResponse().state === 'error');

  updateSearch(value: string): void {
    this.pageIndex.set(0);
    this.searchTerm.set(value);
  }

  toggleSort(column: string): void {
    if (this.sortBy() === column) {
      this.sortDir.update((dir) => (dir === 'asc' ? 'desc' : 'asc'));
    } else {
      this.sortBy.set(column);
      this.sortDir.set('asc');
    }
    this.pageIndex.set(0);
  }

  sortIcon(column: string): string {
    if (this.sortBy() !== column) {
      return 'unfold_more';
    }
    return this.sortDir() === 'asc' ? 'arrow_upward' : 'arrow_downward';
  }

  changePage(page: number): void {
    this.pageIndex.set(page);
  }

  changePageSize(size: number): void {
    this.pageSize.set(size);
    this.pageIndex.set(0);
  }

  openCustomer(account: Account): void {
    if (!account.customerId) {
      return;
    }
    this.router.navigate(['/app/customers', account.customerId]);
  }

  openDeposit(account?: Account): void {
    const dialogRef = this.dialog.open(DepositDialog, {
      width: '420px',
      data: account ? { account } : {}
    });

    dialogRef.afterClosed().subscribe((saved) => {
      if (saved) {
        this.reloadTick.update((n) => n + 1);
      }
    });
  }
  openWithdraw(account?: Account): void {
    const dialogRef = this.dialog.open(WithdrawDialog, {
      width: '420px',
      data: account ? { account } : {}
    });

    dialogRef.afterClosed().subscribe((saved) => {
      if (saved) {
        this.reloadTick.update((n) => n + 1);
      }
    });
  }
  openAccount(account: Account): void {
    this.router.navigate(['/app/accounts', account.accountId]);
  }
  openTransfer(fromAccount?: Account): void {
    const dialogRef = this.dialog.open(TransferDialog, {
      width: '480px',
      data: fromAccount ? { fromAccount } : {}
    });

    dialogRef.afterClosed().subscribe((saved) => {
      if (saved) {
        this.reloadTick.update((n) => n + 1);
      }
    });
  }
}
