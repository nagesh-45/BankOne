import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../config/api-config';
import { PagedResponse } from '../models/paged-response';
import { Transaction } from '../models/transaction';

@Injectable({
  providedIn: 'root'
})
export class TransactionService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_BASE_URL}/transactions`;

  listTransactions(
    search = '',
    page = 0,
    size = 10,
    sortBy = 'createdAt',
    sortDir: 'asc' | 'desc' = 'desc',
    type?: '' | 'CREDIT' | 'DEBIT'
  ): Observable<PagedResponse<Transaction>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);

    if (search.trim()) {
      params = params.set('search', search.trim());
    }
    if (type) {
      params = params.set('type', type);
    }

    return this.http.get<PagedResponse<Transaction>>(this.baseUrl, { params });
  }
}
