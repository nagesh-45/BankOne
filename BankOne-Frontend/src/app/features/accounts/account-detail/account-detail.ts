import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { catchError, combineLatest, map, of, startWith, switchMap } from 'rxjs';

import { Account } from '../../../core/models/account';
import { PagedResponse } from '../../../core/models/paged-response';
import { Transaction } from '../../../core/models/transaction';
import { AccountService } from '../../../core/services/account';
import { ListPagination } from '../../../shared/components/list-pagination/list-pagination';
import { LoadingState } from '../../../shared/components/loading-state/loading-state';
import { AsUtcPipe } from '../../../core/pipes/as-utc.pipe';

type AccountState = {
  state: 'loading' | 'loaded' | 'error';
  account: Account | null;
};

type TxState = {
  state: 'loading' | 'loaded' | 'error';
  page: PagedResponse<Transaction> | null;
};

@Component({
  selector: 'app-account-detail',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    ListPagination,
    LoadingState,
    AsUtcPipe
  ],
  templateUrl: './account-detail.html',
  styleUrl: './account-detail.scss'
})
export class AccountDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly accountService = inject(AccountService);

  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);
  private readonly reloadTick = signal(0);

  private readonly accountId$ = this.route.paramMap.pipe(
    map((params) => Number(params.get('id')))
  );

  private readonly accountResponse = toSignal(
    this.accountId$.pipe(
      switchMap((accountId) => {
        if (!accountId || Number.isNaN(accountId)) {
          return of<AccountState>({ state: 'error', account: null });
        }
        return this.accountService.getById(accountId).pipe(
          map((account) => ({ state: 'loaded' as const, account })),
          startWith({ state: 'loading' as const, account: null as Account | null }),
          catchError(() => of<AccountState>({ state: 'error', account: null }))
        );
      })
    ),
    { initialValue: { state: 'loading' as const, account: null as Account | null } }
  );

  private readonly txResponse = toSignal(
    combineLatest([
      this.accountId$,
      toObservable(this.pageIndex),
      toObservable(this.pageSize),
      toObservable(this.reloadTick)
    ]).pipe(
      switchMap(([accountId, page, size]) => {
        if (!accountId || Number.isNaN(accountId)) {
          return of<TxState>({ state: 'error', page: null });
        }
        return this.accountService.getTransactions(accountId, page, size).pipe(
          map((pageData) => ({ state: 'loaded' as const, page: pageData })),
          startWith({
            state: 'loading' as const,
            page: null as PagedResponse<Transaction> | null
          }),
          catchError(() => of<TxState>({ state: 'error', page: null }))
        );
      })
    ),
    {
      initialValue: {
        state: 'loading' as const,
        page: null as PagedResponse<Transaction> | null
      }
    }
  );

  readonly account = computed(() => this.accountResponse().account);
  readonly isLoadingAccount = computed(() => this.accountResponse().state === 'loading');
  readonly hasAccountError = computed(() => this.accountResponse().state === 'error');

  readonly transactions = computed(() => this.txResponse().page?.content ?? []);
  readonly isLoadingTx = computed(() => this.txResponse().state === 'loading');
  readonly hasTxError = computed(() => this.txResponse().state === 'error');
  readonly totalTx = computed(() => this.txResponse().page?.totalElements ?? 0);
  readonly totalTxPages = computed(() => this.txResponse().page?.totalPages ?? 0);

  onPageIndexChange(pageIndex: number): void {
    this.pageIndex.set(pageIndex);
  }

  onPageSizeChange(pageSize: number): void {
    this.pageSize.set(pageSize);
    this.pageIndex.set(0);
  }
}
