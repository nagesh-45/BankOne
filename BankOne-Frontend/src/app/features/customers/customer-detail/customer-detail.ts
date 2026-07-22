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
import { AccountService } from '../../../core/services/account';
import { CustomerService } from '../../../core/services/customer';
import { Notification } from '../../../core/services/notification';
import { AccountStatusDialog } from '../account-status-dialog/account-status-dialog';

type DetailState = {
  state: 'loading' | 'loaded' | 'error';
  customer: Customer | null;
  accounts: Account[];
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
    MatIconModule
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

  private readonly reloadTick = signal(0);

  private readonly detailResponse = toSignal(
    combineLatest([
      this.route.paramMap.pipe(map((params) => Number(params.get('id')))),
      toObservable(this.reloadTick)
    ]).pipe(
      switchMap(([customerId]) => {
        if (!customerId || Number.isNaN(customerId)) {
          return of<DetailState>({
            state: 'error',
            customer: null,
            accounts: []
          });
        }

        return this.customerService.getCustomerById(customerId).pipe(
          switchMap((customer) =>
            this.accountService.getAccountsByCustomer(customerId).pipe(
              map((accounts) => ({
                state: 'loaded' as const,
                customer,
                accounts
              })),
              catchError((error) => {
                console.error('Failed to load accounts', error);
                return of<DetailState>({
                  state: 'loaded',
                  customer,
                  accounts: []
                });
              })
            )
          ),
          startWith<DetailState>({
            state: 'loading',
            customer: null,
            accounts: []
          }),
          catchError((error) => {
            console.error('Failed to load customer', error);
            return of<DetailState>({
              state: 'error',
              customer: null,
              accounts: []
            });
          })
        );
      })
    ),
    {
      initialValue: {
        state: 'loading',
        customer: null,
        accounts: []
      } satisfies DetailState
    }
  );

  readonly customer = computed(() => this.detailResponse()?.customer ?? null);
  readonly accounts = computed(() => this.detailResponse()?.accounts ?? []);
  readonly isLoading = computed(() => this.detailResponse()?.state === 'loading');
  readonly hasError = computed(() => this.detailResponse()?.state === 'error');

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
