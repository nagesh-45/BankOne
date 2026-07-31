import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { catchError, finalize, forkJoin, of } from 'rxjs';

import { PendingTransfer } from '../../../core/models/portal-transfer';
import { Auth } from '../../../core/services/auth';
import { Notification } from '../../../core/services/notification';
import { PortalService } from '../../../core/services/portal';
import { apiErrorMessage } from '../../../core/utils/api-error-message';
import { LoadingState } from '../../../shared/components/loading-state/loading-state';

@Component({
  selector: 'app-transfer-approvals',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    LoadingState,
    RouterLink
  ],
  templateUrl: './transfer-approvals.html',
  styleUrl: './transfer-approvals.scss'
})
export class TransferApprovals {
  private readonly portal = inject(PortalService);
  private readonly notification = inject(Notification);
  private readonly auth = inject(Auth);

  readonly loading = signal(true);
  readonly items = signal<PendingTransfer[]>([]);
  readonly myHistory = signal<PendingTransfer[]>([]);
  readonly historyLoading = signal(true);
  readonly historyError = signal<string | null>(null);
  readonly currentUsername = this.auth.getUsername();
  readonly canOpenAudit = this.auth.hasAnyRole(['ADMIN', 'MANAGER', 'AUDITOR']);

  constructor() {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.historyLoading.set(true);
    this.historyError.set(null);

    forkJoin({
      pending: this.portal.listPendingApprovals().pipe(
        catchError((error) => {
          this.notification.error(apiErrorMessage(error, 'Failed to load pending approvals'));
          return of([] as PendingTransfer[]);
        })
      ),
      history: this.portal.listMyApprovalHistory().pipe(
        catchError((error) => {
          this.historyError.set(apiErrorMessage(error, 'Failed to load your approval history'));
          return of([] as PendingTransfer[]);
        })
      )
    }).pipe(
      finalize(() => {
        this.loading.set(false);
        this.historyLoading.set(false);
      })
    ).subscribe(({ pending, history }) => {
      this.items.set(pending);
      this.myHistory.set(history);
    });
  }

  approve(id: number): void {
    this.portal.approveTransfer(id).subscribe({
      next: (resolved) => {
        this.notification.success('Transfer approved — see My approval history below');
        this.items.update((list) => list.filter((row) => row.transferRequestId !== id));
        this.prependHistory(resolved);
        this.reload();
      },
      error: (error) => {
        this.notification.error(apiErrorMessage(error, 'Approve failed'));
      }
    });
  }

  reject(id: number): void {
    const reason = window.prompt('Rejection reason (optional)') ?? undefined;
    this.portal.rejectTransfer(id, reason || undefined).subscribe({
      next: (resolved) => {
        this.notification.success('Transfer rejected — see My approval history below');
        this.items.update((list) => list.filter((row) => row.transferRequestId !== id));
        this.prependHistory(resolved);
        this.reload();
      },
      error: (error) => {
        this.notification.error(apiErrorMessage(error, 'Reject failed'));
      }
    });
  }

  private prependHistory(resolved: PendingTransfer): void {
    this.myHistory.update((list) => {
      const without = list.filter((row) => row.transferRequestId !== resolved.transferRequestId);
      return [resolved, ...without];
    });
  }
}
