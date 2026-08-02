import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { finalize } from 'rxjs';

import { Auth } from '../../core/services/auth';
import { Notification } from '../../core/services/notification';
import { PortalService, ReplicaSyncStatus } from '../../core/services/portal';
import {
  CustomerCreateDialog,
  CustomerCreateResult
} from '../customers/customer-create-dialog/customer-create-dialog';
import {
  EmployeeCreateResult,
  UserCreateDialog
} from '../employees/user-create-dialog/user-create-dialog';

@Component({
  selector: 'app-management',
  standalone: true,
  imports: [
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule
  ],
  templateUrl: './management.html',
  styleUrl: './management.scss'
})
export class Management implements OnInit {
  private readonly auth = inject(Auth);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);
  private readonly portal = inject(PortalService);
  private readonly notification = inject(Notification);

  readonly canCreateCustomer = this.auth.can(
    ['CUSTOMERS_WRITE'],
    ['ADMIN', 'EMPLOYEE']
  );
  readonly canCreateEmployee = this.auth.can(['USERS_MANAGE'], ['ADMIN']);
  readonly canViewEmployees = this.auth.can(['USERS_MANAGE'], ['ADMIN']);
  readonly canManageRoles = this.auth.can(['ROLES_MANAGE'], ['ADMIN']);
  readonly canApproveTransfers = this.auth.can(
    ['ACCOUNTS_WRITE'],
    ['ADMIN', 'EMPLOYEE', 'MANAGER']
  );
  readonly canAccessAudit = this.auth.hasAnyRole(['ADMIN', 'MANAGER', 'AUDITOR']);
  /** Staff (not customers) can open policies; only Admin/Manager edit. */
  readonly canViewPolicies = this.auth.can(
    ['POLICIES_MANAGE', 'ACCOUNTS_READ', 'ACCOUNTS_WRITE'],
    ['ADMIN', 'EMPLOYEE', 'MANAGER', 'TELLER', 'AUDITOR']
  );
  readonly canEditPolicies = this.auth.can(['POLICIES_MANAGE'], ['ADMIN', 'MANAGER'])
    && this.auth.hasAnyRole(['ADMIN', 'MANAGER']);
  /** Replica sync is ADMIN-only (matches /admin/replica/**). */
  readonly canSyncReplica = this.auth.hasAnyRole(['ADMIN']);

  readonly replicaSyncing = signal(false);
  readonly replicaStatus = signal<ReplicaSyncStatus | null>(null);
  readonly replicaUnavailable = signal(false);

  ngOnInit(): void {
    if (this.canSyncReplica) {
      this.refreshReplicaStatus();
    }
  }

  refreshReplicaStatus(): void {
    this.portal.getReplicaSyncStatus().subscribe({
      next: (status) => {
        this.replicaUnavailable.set(false);
        this.replicaStatus.set(status);
      },
      error: () => {
        this.replicaUnavailable.set(true);
        this.replicaStatus.set(null);
      }
    });
  }

  runReplicaSync(): void {
    this.replicaSyncing.set(true);
    this.portal.syncReadReplica().pipe(
      finalize(() => this.replicaSyncing.set(false))
    ).subscribe({
      next: (status) => {
        this.replicaUnavailable.set(false);
        this.replicaStatus.set(status);
        const customers = status.rowCounts?.['customers'];
        this.notification.success(
          customers != null
            ? `Replica synced (${customers} customers).`
            : 'Replica synced.'
        );
      },
      error: (err) => {
        if (err?.status === 404) {
          this.replicaUnavailable.set(true);
          this.notification.error('Replica sync is not enabled on this API.');
          return;
        }
        if (err?.status === 403) {
          this.notification.error('Admin role required to sync replica.');
          return;
        }
        this.notification.error(
          err?.error?.message ?? 'Replica sync failed. Check API logs.'
        );
      }
    });
  }

  openCreateCustomer(): void {
    this.dialog.open<CustomerCreateDialog, void, CustomerCreateResult | false>(
      CustomerCreateDialog,
      {
        width: '640px',
        maxWidth: '95vw',
        disableClose: true
      }
    ).afterClosed().subscribe((result) => {
      if (result && result.action === 'view') {
        this.router.navigate(['/app/customers', result.customerId]);
      }
    });
  }

  openCreateEmployee(): void {
    this.dialog.open<UserCreateDialog, void, EmployeeCreateResult | false>(
      UserCreateDialog,
      {
        width: '640px',
        maxWidth: '95vw',
        disableClose: true
      }
    ).afterClosed().subscribe((result) => {
      if (result && result.action === 'view') {
        this.router.navigate(['/app/employees'], {
          queryParams: { q: result.employeeCode }
        });
      }
    });
  }
}
