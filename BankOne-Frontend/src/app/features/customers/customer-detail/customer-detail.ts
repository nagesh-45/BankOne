import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { catchError, combineLatest, map, of, startWith, switchMap } from 'rxjs';

import { Account } from '../../../core/models/account';
import { Customer } from '../../../core/models/customer';
import { PagedResponse } from '../../../core/models/paged-response';
import { AccountService } from '../../../core/services/account';
import { CustomerService } from '../../../core/services/customer';
import { Notification } from '../../../core/services/notification';
import { ListPagination } from '../../../shared/components/list-pagination/list-pagination';
import { BusinessIdPipe } from '../../../core/pipes/business-id.pipe';
import { AccountStatusDialog } from '../account-status-dialog/account-status-dialog';

type DetailState = {
  state: 'loading' | 'loaded' | 'error';
  customer: Customer | null;
  accountsPage: PagedResponse<Account> | null;
};

@Component({
  selector: 'app-customer-detail',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    ListPagination,
    BusinessIdPipe
  ],
  templateUrl: './customer-detail.html',
  styleUrl: './customer-detail.scss'
})
export class CustomerDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly customerService = inject(CustomerService);
  private readonly accountService = inject(AccountService);
  private readonly notification = inject(Notification);
  private readonly dialog = inject(MatDialog);

  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);
  private readonly reloadTick = signal(0);

  private readonly detailResponse = toSignal(
    combineLatest([
      this.route.paramMap.pipe(map((params) => Number(params.get('id')))),
      toObservable(this.pageIndex),
      toObservable(this.pageSize),
      toObservable(this.reloadTick)
    ]).pipe(
      switchMap(([customerId, page, size]) => {
        if (!customerId || Number.isNaN(customerId)) {
          return of<DetailState>({
            state: 'error',
            customer: null,
            accountsPage: null
          });
        }

        return this.customerService.getCustomerById(customerId).pipe(
          switchMap((customer) =>
            this.accountService.getAccountsByCustomer(customerId, page, size).pipe(
              map((accountsPage) => ({
                state: 'loaded' as const,
                customer,
                accountsPage
              })),
              catchError((error) => {
                console.error('Failed to load accounts', error);
                return of<DetailState>({
                  state: 'loaded',
                  customer,
                  accountsPage: null
                });
              })
            )
          ),
          startWith<DetailState>({
            state: 'loading',
            customer: null,
            accountsPage: null
          }),
          catchError((error) => {
            console.error('Failed to load customer', error);
            return of<DetailState>({
              state: 'error',
              customer: null,
              accountsPage: null
            });
          })
        );
      })
    ),
    {
      initialValue: {
        state: 'loading',
        customer: null,
        accountsPage: null
      } satisfies DetailState
    }
  );

  readonly customer = computed(() => this.detailResponse()?.customer ?? null);
  readonly accounts = computed(() => this.detailResponse()?.accountsPage?.content ?? []);
  readonly totalAccounts = computed(
    () => this.detailResponse()?.accountsPage?.totalElements ?? 0
  );
  readonly totalPages = computed(
    () => this.detailResponse()?.accountsPage?.totalPages ?? 0
  );
  readonly isLoading = computed(() => this.detailResponse()?.state === 'loading');
  readonly hasError = computed(() => this.detailResponse()?.state === 'error');

  changePage(page: number): void {
    this.pageIndex.set(page);
  }

  changePageSize(size: number): void {
    this.pageSize.set(size);
    this.pageIndex.set(0);
  }

  addLoanAccount(): void {
    const customer = this.customer();
    if (!customer) {
      return;
    }

    this.accountService.openAccount({
      customerId: customer.customerId,
      branchCode: '0001',
      accountType: 'LOAN',
      currencyCode: 'INR',
      openingDeposit: 1,
      createdBy: 'SYSTEM'
    }).subscribe({
      next: () => {
        this.notification.success('Loan account added successfully');
        this.reloadTick.update((value) => value + 1);
      },
      error: (error) => {
        this.notification.error(
          error.error?.message ?? 'Failed to add loan account'
        );
      }
    });
  }

  editAccountStatus(account: Account): void {
    const dialogRef = this.dialog.open(AccountStatusDialog, {
      width: '420px',
      maxWidth: '95vw',
      disableClose: true,
      data: { account }
    });

    dialogRef.afterClosed().subscribe((updated) => {
      if (updated) {
        this.reloadTick.update((value) => value + 1);
      }
    });
  }
}
