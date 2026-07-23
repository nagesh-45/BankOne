import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../config/api-config';
import { Account } from '../models/account';
import { PagedResponse } from '../models/paged-response';

export interface OpenAccountRequest {
  customerId: number;
  branchCode: string;
  accountType: string;
  currencyCode: string;
  openingDeposit: number;
  createdBy: string;
}

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_BASE_URL}/accounts`;

  getAccounts(
    search = '',
    page = 0,
    size = 10,
    sortBy = 'createdAt',
    sortDir: 'asc' | 'desc' = 'desc'
  ): Observable<PagedResponse<Account>> {
    const params = new HttpParams()
      .set('search', search)
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);

    return this.http.get<PagedResponse<Account>>(this.baseUrl, { params });
  }

  getAccountsByCustomer(
    customerId: number,
    page = 0,
    size = 10,
    sortBy = 'createdAt',
    sortDir: 'asc' | 'desc' = 'desc'
  ): Observable<PagedResponse<Account>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);

    return this.http.get<PagedResponse<Account>>(
      `${this.baseUrl}/customer/${customerId}`,
      { params }
    );
  }

  openAccount(request: OpenAccountRequest): Observable<Account> {
    return this.http.post<Account>(this.baseUrl, request);
  }

  updateAccountStatus(accountId: number, status: string): Observable<Account> {
    return this.http.put<Account>(`${this.baseUrl}/${accountId}/status`, { status });
  }
  deposit(accountId: number, amount: number): Observable<Account> {
    return this.http.post<Account>(`${this.baseUrl}/${accountId}/deposit`, {
      amount
    });
  }
}
