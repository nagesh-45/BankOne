import { Component, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
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

import { Auth } from '../../../core/services/auth';
import { Customer } from '../../../core/models/customer';
import { PagedResponse } from '../../../core/models/paged-response';
import { CustomerService } from '../../../core/services/customer';
import { ListPagination } from '../../../shared/components/list-pagination/list-pagination';
import { BusinessIdPipe } from '../../../core/pipes/business-id.pipe';

@Component({
  selector: 'app-customer-list',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    ListPagination,
    BusinessIdPipe
  ],
  templateUrl: './customer-list.html',
  styleUrl: './customer-list.scss',
})
export class CustomerList {
  private readonly auth = inject(Auth);
  private readonly customerService = inject(CustomerService);
  private readonly router = inject(Router);

  readonly canCreateCustomer = this.auth.hasAnyRole(['ADMIN', 'EMPLOYEE']);

  readonly searchTerm = signal('');
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);
  readonly sortBy = signal('customerId');
  readonly sortDir = signal<'asc' | 'desc'>('desc');

  private readonly customerResponse = toSignal(
    combineLatest([
      toObservable(this.searchTerm).pipe(
        debounceTime(250),
        distinctUntilChanged()
      ),
      toObservable(this.pageIndex),
      toObservable(this.pageSize),
      toObservable(this.sortBy),
      toObservable(this.sortDir)
    ]).pipe(
      switchMap(([search, page, size, sortBy, sortDir]) =>
        this.customerService.getCustomers(search.trim(), page, size, sortBy, sortDir).pipe(
          map((response) => ({
            state: 'loaded' as const,
            response
          })),
          startWith({
            state: 'loading' as const,
            response: null as PagedResponse<Customer> | null
          }),
          catchError((error) => {
            console.error('Failed to load customers', error);
            return of({
              state: 'error' as const,
              response: null as PagedResponse<Customer> | null
            });
          })
        )
      )
    ),
    {
      initialValue: {
        state: 'loading' as const,
        response: null as PagedResponse<Customer> | null
      }
    }
  );

  readonly customers = computed(() => this.customerResponse().response?.content ?? []);
  readonly totalCustomers = computed(
    () => this.customerResponse().response?.totalElements ?? 0
  );
  readonly totalPages = computed(
    () => this.customerResponse().response?.totalPages ?? 0
  );
  readonly isLoading = computed(() => this.customerResponse().state === 'loading');
  readonly hasError = computed(() => this.customerResponse().state === 'error');

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

  openCustomer(customer: Customer): void {
    this.router.navigate(['/app/customers', customer.customerId]);
  }
}
