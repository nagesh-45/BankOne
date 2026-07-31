import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import {
  catchError,
  combineLatest,
  map,
  of,
  switchMap
} from 'rxjs';

import { Account } from '../../../core/models/account';
import { PagedResponse } from '../../../core/models/paged-response';
import { Transaction } from '../../../core/models/transaction';
import { PortalService } from '../../../core/services/portal';
import { ListPagination } from '../../../shared/components/list-pagination/list-pagination';
import { LoadingState } from '../../../shared/components/loading-state/loading-state';

type AccountView = {
  loading: boolean;
  error: boolean;
  account: Account | null;
};

type TxView = {
  loading: boolean;
  page: PagedResponse<Transaction> | null;
};

@Component({
  selector: 'app-my-account-detail',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    ListPagination,
    LoadingState
  ],
  templateUrl: './my-account-detail.html',
  styleUrl: './my-account-detail.scss'
})
export class MyAccountDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly portal = inject(PortalService);

  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);

  private readonly accountId$ = this.route.paramMap.pipe(
    map((params) => Number(params.get('id')))
  );

  private readonly accountView = toSignal(
    this.accountId$.pipe(
      switchMap((id) => {
        if (!id || Number.isNaN(id)) {
          return of<AccountView>({ loading: false, error: true, account: null });
        }
        return this.portal.getMyAccount(id).pipe(
          map((account) => ({ loading: false, error: false, account })),
          catchError(() => of({ loading: false, error: true, account: null }))
        );
      })
    ),
    { initialValue: { loading: true, error: false, account: null } satisfies AccountView }
  );

  private readonly txView = toSignal(
    combineLatest([
      this.accountId$,
      toObservable(this.pageIndex),
      toObservable(this.pageSize)
    ]).pipe(
      switchMap(([id, page, size]) => {
        if (!id || Number.isNaN(id)) {
          return of<TxView>({ loading: false, page: null });
        }
        return this.portal.getMyTransactions(id, page, size).pipe(
          map((pageData) => ({ loading: false, page: pageData })),
          catchError(() => of({ loading: false, page: null }))
        );
      })
    ),
    { initialValue: { loading: true, page: null } satisfies TxView }
  );

  readonly account = computed(() => this.accountView().account);
  readonly accountLoading = computed(() => this.accountView().loading);
  readonly accountError = computed(() => this.accountView().error);
  readonly transactions = computed(() => this.txView().page?.content ?? []);
  readonly totalTx = computed(() => this.txView().page?.totalElements ?? 0);
  readonly totalPages = computed(() => this.txView().page?.totalPages ?? 0);
  readonly txLoading = computed(() => this.txView().loading);

  onPageChange(page: number): void {
    this.pageIndex.set(page);
  }

  onPageSizeChange(size: number): void {
    this.pageSize.set(size);
    this.pageIndex.set(0);
  }
}
