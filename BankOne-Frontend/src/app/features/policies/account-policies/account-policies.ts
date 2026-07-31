import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { catchError, finalize, of } from 'rxjs';

import { AccountPolicy } from '../../../core/models/account-policy';
import { AccountPolicyService } from '../../../core/services/account-policy';
import { Auth } from '../../../core/services/auth';
import { Notification } from '../../../core/services/notification';
import { LoadingState } from '../../../shared/components/loading-state/loading-state';
import {
  PolicyEditDialog,
  PolicyEditDialogData
} from '../policy-edit-dialog/policy-edit-dialog';

@Component({
  selector: 'app-account-policies',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatDialogModule,
    MatIconModule,
    LoadingState
  ],
  templateUrl: './account-policies.html',
  styleUrl: './account-policies.scss'
})
export class AccountPolicies {
  private readonly accountPolicyService = inject(AccountPolicyService);
  private readonly auth = inject(Auth);
  private readonly notification = inject(Notification);
  private readonly dialog = inject(MatDialog);

  readonly loading = signal(true);
  readonly error = signal(false);
  readonly policies = signal<AccountPolicy[]>([]);

  /** Admin / Manager may edit; other staff view only. Customers never reach this page. */
  readonly canEdit = this.auth.can(['POLICIES_MANAGE'], ['ADMIN', 'MANAGER'])
    && this.auth.hasAnyRole(['ADMIN', 'MANAGER']);

  constructor() {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(false);
    this.accountPolicyService.listAll().pipe(
      catchError(() => {
        this.error.set(true);
        return of([] as AccountPolicy[]);
      }),
      finalize(() => this.loading.set(false))
    ).subscribe((items) => this.policies.set(items));
  }

  edit(policy: AccountPolicy): void {
    if (!this.canEdit) {
      return;
    }

    this.dialog.open<PolicyEditDialog, PolicyEditDialogData, AccountPolicy | false>(
      PolicyEditDialog,
      {
        width: '520px',
        maxWidth: '95vw',
        disableClose: true,
        data: { policy }
      }
    ).afterClosed().subscribe((result) => {
      if (result) {
        this.notification.success('Policy updated');
        this.reload();
      }
    });
  }
}
