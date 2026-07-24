import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { catchError, combineLatest, map, of, startWith, switchMap } from 'rxjs';
import { AccountPolicyService } from '../../../core/services/account-policy';
import { OpeningDepositDialog } from '../../opening-deposit-dialog/opening-deposit-dialog';
import { apiErrorMessage } from '../../../core/utils/api-error-message';
import { Account } from '../../../core/models/account';
import { Customer } from '../../../core/models/customer';
import { PagedResponse } from '../../../core/models/paged-response';
import { AccountService } from '../../../core/services/account';
import { Auth } from '../../../core/services/auth';
import { CustomerService } from '../../../core/services/customer';
import { Notification } from '../../../core/services/notification';
import { ListPagination } from '../../../shared/components/list-pagination/list-pagination';
import { LoadingState } from '../../../shared/components/loading-state/loading-state';
import { BusinessIdPipe } from '../../../core/pipes/business-id.pipe';
import { AccountStatusDialog } from '../account-status-dialog/account-status-dialog';
import { CustomerEditDialog } from '../customer-edit-dialog/customer-edit-dialog';

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
    LoadingState,
    BusinessIdPipe
  ],
  templateUrl: './customer-detail.html',
  styleUrl: './customer-detail.scss'
})
export class CustomerDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly auth = inject(Auth);
  private readonly customerService = inject(CustomerService);
  private readonly accountService = inject(AccountService);
  private readonly accountPolicyService = inject(AccountPolicyService);
  private readonly notification = inject(Notification);
  private readonly dialog = inject(MatDialog);

  readonly canEditCustomer = this.auth.hasAnyRole(['ADMIN', 'EMPLOYEE']);

  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);
  readonly sortBy = signal('createdAt');
  readonly sortDir = signal<'asc' | 'desc'>('desc');
  private readonly reloadTick = signal(0);

  private readonly detailResponse = toSignal(
    combineLatest([
      this.route.paramMap.pipe(map((params) => Number(params.get('id')))),
      toObservable(this.pageIndex),
      toObservable(this.pageSize),
      toObservable(this.sortBy),
      toObservable(this.sortDir),
      toObservable(this.reloadTick)
    ]).pipe(
      switchMap(([customerId, page, size, sortBy, sortDir]) => {
        if (!customerId || Number.isNaN(customerId)) {
          return of<DetailState>({
            state: 'error',
            customer: null,
            accountsPage: null
          });
        }

        return this.customerService.getCustomerById(customerId).pipe(
          switchMap((customer) =>
            this.accountService.getAccountsByCustomer(
              customerId,
              page,
              size,
              sortBy,
              sortDir
            ).pipe(
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

  editCustomer(): void {
    const customer = this.customer();
    if (!customer) {
      return;
    }

    const dialogRef = this.dialog.open(CustomerEditDialog, {
      width: '520px',
      maxWidth: '95vw',
      disableClose: true,
      data: { customer }
    });

    dialogRef.afterClosed().subscribe((updated) => {
      if (updated) {
        this.reloadTick.update((value) => value + 1);
      }
    });
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

  addCurrentAccount(): void {
    const customer = this.customer();
    if (!customer) {
      return;
    }

    const accountType = 'CURRENT';
    const currencyCode = 'INR';

    this.accountPolicyService.getActivePolicy(accountType, currencyCode).subscribe({
      next: (policy) => {
        if (policy.openingDepositRequired) {
          const dialogRef = this.dialog.open(OpeningDepositDialog, {
            width: '420px',
            disableClose: true,
            data: {
              accountType,
              currencyCode,
              requiredOpeningDeposit: policy.requiredOpeningDeposit
            }
          });

          dialogRef.afterClosed().subscribe((amount) => {
            if (amount === false || amount == null) {
              return;
            }
            this.createAccount(
              customer.customerId,
              accountType,
              currencyCode,
              Number(amount)
            );
          });
          return;
        }

        this.createAccount(customer.customerId, accountType, currencyCode, 0);
      },
      error: (error) => {
        this.notification.error(
          apiErrorMessage(error, 'Failed to load account policy')
        );
      }
    });
  }

  private createAccount(
    customerId: number,
    accountType: string,
    currencyCode: string,
    openingDeposit: number
  ): void {
    this.accountService
      .openAccount({
        customerId,
        branchCode: '0001',
        accountType,
        currencyCode,
        openingDeposit,
        createdBy: 'SYSTEM'
      })
      .subscribe({
        next: () => {
          this.notification.success(`${accountType} account added successfully`);
          this.reloadTick.update((value) => value + 1);
        },
        error: (error) => {
          this.notification.error(
            apiErrorMessage(error, `Failed to add ${accountType} account`)
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
