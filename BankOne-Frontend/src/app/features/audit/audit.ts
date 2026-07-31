import { HttpErrorResponse } from '@angular/common/http';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { catchError, finalize, of } from 'rxjs';

import { AuditCategory, AuditEvent } from '../../core/models/audit-event';
import { PagedResponse } from '../../core/models/paged-response';
import { PendingTransfer } from '../../core/models/portal-transfer';
import { PortalService } from '../../core/services/portal';
import { ListPagination } from '../../shared/components/list-pagination/list-pagination';
import { LoadingState } from '../../shared/components/loading-state/loading-state';

type AuditTab = 'ALL' | AuditCategory | 'APPROVALS';

const EMPTY_PAGE: PagedResponse<AuditEvent> = {
  content: [],
  totalElements: 0,
  totalPages: 0,
  size: 25,
  number: 0,
  numberOfElements: 0,
  first: true,
  last: true,
  empty: true
};

@Component({
  selector: 'app-audit',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    ListPagination,
    LoadingState
  ],
  templateUrl: './audit.html',
  styleUrl: './audit.scss'
})
export class Audit {
  private readonly portal = inject(PortalService);

  readonly tabs: { id: AuditTab; label: string }[] = [
    { id: 'ALL', label: 'All activity' },
    { id: 'AUTH', label: 'Login / Logout' },
    { id: 'CUSTOMER', label: 'Customer' },
    { id: 'ACCOUNT', label: 'Account' },
    { id: 'TRANSFER', label: 'Transfer' },
    { id: 'PORTAL', label: 'Portal' },
    { id: 'STAFF', label: 'Staff' },
    { id: 'ROLE', label: 'Role' },
    { id: 'POLICY', label: 'Policy' },
    { id: 'APPROVALS', label: 'Approval history' }
  ];

  readonly activeTab = signal<AuditTab>('ALL');
  readonly actorFilter = signal('');
  readonly pageIndex = signal(0);
  readonly pageSize = 25;

  readonly loading = signal(true);
  readonly error = signal(false);
  readonly events = signal<AuditEvent[]>([]);
  readonly total = signal(0);
  readonly totalPages = signal(0);
  readonly approvals = signal<PendingTransfer[]>([]);
  readonly backfilling = signal(false);
  readonly backfillMessage = signal<string | null>(null);

  readonly showingApprovals = computed(() => this.activeTab() === 'APPROVALS');

  constructor() {
    this.reload();
  }

  selectTab(tab: AuditTab): void {
    this.activeTab.set(tab);
    this.pageIndex.set(0);
    this.reload();
  }

  onActorChange(value: string): void {
    this.actorFilter.set(value);
  }

  applyActorFilter(): void {
    this.pageIndex.set(0);
    this.reload();
  }

  onPageChange(pageIndex: number): void {
    this.pageIndex.set(pageIndex);
    this.reload();
  }

  runBackfill(): void {
    this.backfilling.set(true);
    this.backfillMessage.set(null);
    this.portal.backfillAuditHistory().pipe(
      catchError((err: unknown) => {
        const http = err instanceof HttpErrorResponse ? err : null;
        if (http?.status === 403) {
          this.backfillMessage.set('Backfill failed: Admin role required.');
        } else if (http?.status === 401) {
          this.backfillMessage.set('Backfill failed: session expired — log in again.');
        } else if (http?.status === 0) {
          this.backfillMessage.set('Backfill failed: API not reachable on :9080.');
        } else if (http?.status) {
          this.backfillMessage.set(
            `Backfill failed: HTTP ${http.status}`
              + (typeof http.error?.error === 'string' ? ` — ${http.error.error}` : '')
              + '. Check Liberty logs.'
          );
        } else {
          this.backfillMessage.set('Backfill failed. Check API and Liberty logs.');
        }
        return of(null);
      }),
      finalize(() => this.backfilling.set(false))
    ).subscribe((result) => {
      if (!result) {
        return;
      }
      const parts = Object.entries(result.insertedBySource ?? {})
        .map(([k, v]) => `${k}: ${v}`)
        .join(', ');
      this.backfillMessage.set(
        `Backfill done — inserted ${result.inserted}, skipped ${result.skipped}`
          + (parts ? ` (${parts})` : '')
          + '. Login/logout history cannot be recovered.'
      );
      this.reload();
    });
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(false);

    if (this.activeTab() === 'APPROVALS') {
      this.portal.listAuditApprovalHistory().pipe(
        catchError(() => {
          this.error.set(true);
          return of([] as PendingTransfer[]);
        }),
        finalize(() => this.loading.set(false))
      ).subscribe((items) => {
        this.approvals.set(items);
        this.events.set([]);
        this.total.set(items.length);
        this.totalPages.set(1);
      });
      return;
    }

    const category = this.activeTab() === 'ALL' ? undefined : this.activeTab();
    this.portal.listAuditEvents({
      category,
      actor: this.actorFilter().trim() || undefined,
      page: this.pageIndex(),
      size: this.pageSize
    }).pipe(
      catchError(() => {
        this.error.set(true);
        return of(EMPTY_PAGE);
      }),
      finalize(() => this.loading.set(false))
    ).subscribe((page) => {
      this.events.set(page.content ?? []);
      this.total.set(page.totalElements ?? 0);
      this.totalPages.set(page.totalPages ?? 0);
      this.approvals.set([]);
    });
  }
}
