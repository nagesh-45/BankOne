import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../config/api-config';
import {
  AccountMixReport,
  ApprovalsReport,
  TransactionTrendsReport
} from '../models/report';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_BASE_URL}/reports`;

  transactionTrends(from: string, to: string): Observable<TransactionTrendsReport> {
    return this.http.get<TransactionTrendsReport>(`${this.baseUrl}/transaction-trends`, {
      params: this.dateParams(from, to)
    });
  }

  accountMix(): Observable<AccountMixReport> {
    return this.http.get<AccountMixReport>(`${this.baseUrl}/account-mix`);
  }

  approvals(from: string, to: string): Observable<ApprovalsReport> {
    return this.http.get<ApprovalsReport>(`${this.baseUrl}/approvals`, {
      params: this.dateParams(from, to)
    });
  }

  downloadPdf(
    category: 'transaction-trends' | 'account-mix' | 'approvals',
    from?: string,
    to?: string
  ): Observable<Blob> {
    let params = new HttpParams();
    if (from) {
      params = params.set('from', from);
    }
    if (to) {
      params = params.set('to', to);
    }
    return this.http.get(`${this.baseUrl}/${category}/pdf`, {
      params,
      responseType: 'blob'
    });
  }

  private dateParams(from: string, to: string): HttpParams {
    return new HttpParams().set('from', from).set('to', to);
  }
}
