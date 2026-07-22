import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { AppUser } from '../models/app-user';
import { CreateUserRequest } from '../models/create-user-request';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/users';

  getEmployees(search = ''): Observable<AppUser[]> {
    const params = new HttpParams().set('search', search);
    return this.http.get<AppUser[]>(this.baseUrl, { params });
  }

  createUser(request: CreateUserRequest): Observable<AppUser> {
    return this.http.post<AppUser>(this.baseUrl, request);
  }
}
