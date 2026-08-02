import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../config/api-config';
import { Account } from '../models/account';
import { AuditEvent } from '../models/audit-event';
import { PagedResponse } from '../models/paged-response';
import {
  Beneficiary,
  CreateBeneficiaryRequest,
  PendingTransfer,
  PortalTransferRequest,
  TransferOutcome
} from '../models/portal-transfer';
import { Transaction } from '../models/transaction';

export interface ReplicaSyncStatus {
  lastSyncAt: string | null;
  message: string;
  rowCounts: Record<string, number>;
}

@Injectable({
  providedIn: 'root'
})
export class PortalService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_BASE_URL}/portal`;
  private readonly approvalsUrl = `${API_BASE_URL}/transfer-approvals`;

  getMyAccounts(): Observable<Account[]> {
    return this.http.get<Account[]>(`${this.baseUrl}/accounts`);
  }

  getMyAccount(accountId: number): Observable<Account> {
    return this.http.get<Account>(`${this.baseUrl}/accounts/${accountId}`);
  }

  getMyTransactions(
    accountId: number,
    page = 0,
    size = 10,
    sortBy = 'createdAt',
    sortDir: 'asc' | 'desc' = 'desc'
  ): Observable<PagedResponse<Transaction>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);

    return this.http.get<PagedResponse<Transaction>>(
      `${this.baseUrl}/accounts/${accountId}/transactions`,
      { params }
    );
  }

  transfer(fromAccountId: number, request: PortalTransferRequest): Observable<TransferOutcome> {
    return this.http.post<TransferOutcome>(
      `${this.baseUrl}/accounts/${fromAccountId}/transfer`,
      request
    );
  }

  listBeneficiaries(): Observable<Beneficiary[]> {
    return this.http.get<Beneficiary[]>(`${this.baseUrl}/beneficiaries`);
  }

  createBeneficiary(request: CreateBeneficiaryRequest): Observable<Beneficiary> {
    return this.http.post<Beneficiary>(`${this.baseUrl}/beneficiaries`, request);
  }

  deleteBeneficiary(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/beneficiaries/${id}`);
  }

  listPendingApprovals(): Observable<PendingTransfer[]> {
    return this.http.get<PendingTransfer[]>(this.approvalsUrl);
  }

  listMyApprovalHistory(): Observable<PendingTransfer[]> {
    return this.http.get<PendingTransfer[]>(`${this.approvalsUrl}/my-history`);
  }

  listAuditApprovalHistory(): Observable<PendingTransfer[]> {
    return this.http.get<PendingTransfer[]>(`${API_BASE_URL}/audit/transfer-approvals`);
  }

  listAuditEvents(params: {
    category?: string;
    action?: string;
    actor?: string;
    page?: number;
    size?: number;
  } = {}): Observable<PagedResponse<AuditEvent>> {
    let httpParams = new HttpParams()
      .set('page', String(params.page ?? 0))
      .set('size', String(params.size ?? 25));
    if (params.category) {
      httpParams = httpParams.set('category', params.category);
    }
    if (params.action) {
      httpParams = httpParams.set('action', params.action);
    }
    if (params.actor) {
      httpParams = httpParams.set('actor', params.actor);
    }
    return this.http.get<PagedResponse<AuditEvent>>(`${API_BASE_URL}/audit/events`, {
      params: httpParams
    });
  }

  backfillAuditHistory(): Observable<{ inserted: number; skipped: number; insertedBySource: Record<string, number> }> {
    return this.http.post<{ inserted: number; skipped: number; insertedBySource: Record<string, number> }>(
      `${API_BASE_URL}/audit/backfill`,
      {}
    );
  }

  syncReadReplica(): Observable<ReplicaSyncStatus> {
    return this.http.post<ReplicaSyncStatus>(`${API_BASE_URL}/admin/replica/sync`, {});
  }

  getReplicaSyncStatus(): Observable<ReplicaSyncStatus> {
    return this.http.get<ReplicaSyncStatus>(`${API_BASE_URL}/admin/replica/status`);
  }

  approveTransfer(id: number): Observable<PendingTransfer> {
    return this.http.post<PendingTransfer>(`${this.approvalsUrl}/${id}/approve`, {});
  }

  rejectTransfer(id: number, rejectionReason?: string): Observable<PendingTransfer> {
    return this.http.post<PendingTransfer>(`${this.approvalsUrl}/${id}/reject`, {
      rejectionReason
    });
  }
}
