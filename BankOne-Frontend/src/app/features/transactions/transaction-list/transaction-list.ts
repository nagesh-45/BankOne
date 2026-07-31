import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
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

import { PagedResponse } from '../../../core/models/paged-response';
import { Transaction } from '../../../core/models/transaction';
import { TransactionService } from '../../../core/services/transaction';
import { BusinessIdPipe } from '../../../core/pipes/business-id.pipe';
import { ListPagination } from '../../../shared/components/list-pagination/list-pagination';
import { LoadingState } from '../../../shared/components/loading-state/loading-state';

@Component({
  selector: 'app-transaction-list',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    FormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    ListPagination,
    LoadingState,
    BusinessIdPipe
  ],
  templateUrl: './transaction-list.html',
  styleUrl: './transaction-list.scss'
})
export class TransactionList {
  private readonly transactionService = inject(TransactionService);

  readonly searchTerm = signal('');
  readonly typeFilter = signal<'' | 'CREDIT' | 'DEBIT'>('');
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);
  readonly sortBy = signal('createdAt');
  readonly sortDir = signal<'asc' | 'desc'>('desc');
  private readonly reloadTick = signal(0);

  private readonly txResponse = toSignal(
    combineLatest([
      toObservable(this.searchTerm).pipe(debounceTime(250), distinctUntilChanged()),
      toObservable(this.typeFilter),
      toObservable(this.pageIndex),
      toObservable(this.pageSize),
      toObservable(this.sortBy),
      toObservable(this.sortDir),
      toObservable(this.reloadTick)
    ]).pipe(
      switchMap(([search, type, page, size, sortBy, sortDir]) =>
        this.transactionService.listTransactions(search, page, size, sortBy, sortDir, type || undefined).pipe(
          map((response) => ({ state: 'loaded' as const, response })),
          startWith({
            state: 'loading' as const,
            response: null as PagedResponse<Transaction> | null
          }),
          catchError(() =>
            of({
              state: 'error' as const,
              response: null as PagedResponse<Transaction> | null
            })
          )
        )
      )
    ),
    {
      initialValue: {
        state: 'loading' as const,
        response: null as PagedResponse<Transaction> | null
      }
    }
  );

  readonly transactions = computed(() => this.txResponse().response?.content ?? []);
  readonly total = computed(() => this.txResponse().response?.totalElements ?? 0);
  readonly totalPages = computed(() => this.txResponse().response?.totalPages ?? 0);
  readonly isLoading = computed(() => this.txResponse().state === 'loading');
  readonly hasError = computed(() => this.txResponse().state === 'error');

  updateSearch(value: string): void {
    this.pageIndex.set(0);
    this.searchTerm.set(value);
  }

  setTypeFilter(value: '' | 'CREDIT' | 'DEBIT'): void {
    this.pageIndex.set(0);
    this.typeFilter.set(value);
  }

  toggleSort(column: string): void {
    if (this.sortBy() === column) {
      this.sortDir.update((dir) => (dir === 'asc' ? 'desc' : 'asc'));
    } else {
      this.sortBy.set(column);
      this.sortDir.set('desc');
    }
  }

  sortIcon(column: string): string {
    if (this.sortBy() !== column) {
      return 'unfold_more';
    }
    return this.sortDir() === 'asc' ? 'arrow_upward' : 'arrow_downward';
  }

  onPageChange(page: number): void {
    this.pageIndex.set(page);
  }

  onPageSizeChange(size: number): void {
    this.pageIndex.set(0);
    this.pageSize.set(size);
  }

  reload(): void {
    this.reloadTick.update((n) => n + 1);
  }
}
