import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../config/api-config';
import { AccountPolicy } from '../models/account-policy';

@Injectable({
  providedIn: 'root'
})
export class AccountPolicyService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_BASE_URL}/account-policies`;

  getActivePolicy(
    accountType: string,
    currencyCode = 'INR'
  ): Observable<AccountPolicy> {
    const params = new HttpParams()
      .set('accountType', accountType)
      .set('currencyCode', currencyCode);

    return this.http.get<AccountPolicy>(this.baseUrl, { params });
  }
}
