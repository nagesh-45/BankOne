import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Account } from '../models/account';

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

  getAccountsByCustomer(customerId: number): Observable<Account[]> {
    return this.http.get<Account[]>(`${this.baseUrl}/customer/${customerId}`);
  }

  openAccount(request: OpenAccountRequest): Observable<Account> {
    return this.http.post<Account>(this.baseUrl, request);
  }

  updateAccountStatus(accountId: number, status: string): Observable<Account> {
    return this.http.put<Account>(`${this.baseUrl}/${accountId}/status`, { status });
  }
}
