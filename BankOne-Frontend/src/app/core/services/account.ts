import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

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
  private readonly baseUrl = 'http://localhost:8080/accounts';

  getAccountsByCustomer(
    customerId: number,
    page = 0,
    size = 10
  ): Observable<PagedResponse<Account>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);

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
}
