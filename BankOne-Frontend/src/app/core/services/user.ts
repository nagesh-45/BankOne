import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { AppUser } from '../models/app-user';
import { CreateUserRequest } from '../models/create-user-request';
import { PagedResponse } from '../models/paged-response';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/users';

  getEmployees(search = '', page = 0, size = 10): Observable<PagedResponse<AppUser>> {
    const params = new HttpParams()
      .set('search', search)
      .set('page', page)
      .set('size', size);

    return this.http.get<PagedResponse<AppUser>>(this.baseUrl, { params });
  }

  createUser(request: CreateUserRequest): Observable<AppUser> {
    return this.http.post<AppUser>(this.baseUrl, request);
  }
}
