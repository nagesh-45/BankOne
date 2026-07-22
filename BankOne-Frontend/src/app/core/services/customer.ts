import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { CreateCustomerRequest } from '../models/create-customer-request';
import { Customer } from '../models/customer';
import { PagedResponse } from '../models/paged-response';

@Injectable({
  providedIn: 'root'
})
export class CustomerService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/customers';

  getCustomers(search = '', page = 0, size = 10): Observable<PagedResponse<Customer>> {
    const params = new HttpParams()
      .set('search', search)
      .set('page', page)
      .set('size', size);

    return this.http.get<PagedResponse<Customer>>(this.baseUrl, { params });
  }

  getCustomerById(customerId: number): Observable<Customer> {
    return this.http.get<Customer>(`${this.baseUrl}/${customerId}`);
  }

  createCustomer(request: CreateCustomerRequest): Observable<Customer> {
    return this.http.post<Customer>(this.baseUrl, request);
  }
}
