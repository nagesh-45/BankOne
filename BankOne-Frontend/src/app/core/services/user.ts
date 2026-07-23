import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../config/api-config';
import { AppUser } from '../models/app-user';
import { CreateUserRequest } from '../models/create-user-request';
import { PagedResponse } from '../models/paged-response';
import { UpdateUserRequest } from '../models/update-user-request';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_BASE_URL}/users`;

  getEmployees(
    search = '',
    page = 0,
    size = 10,
    sortBy = 'userId',
    sortDir: 'asc' | 'desc' = 'desc'
  ): Observable<PagedResponse<AppUser>> {
    const params = new HttpParams()
      .set('search', search)
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);

    return this.http.get<PagedResponse<AppUser>>(this.baseUrl, { params });
  }

  createUser(request: CreateUserRequest): Observable<AppUser> {
    return this.http.post<AppUser>(this.baseUrl, request);
  }

  updateUser(userId: number, request: UpdateUserRequest): Observable<AppUser> {
    return this.http.put<AppUser>(`${this.baseUrl}/${userId}`, request);
  }
}
