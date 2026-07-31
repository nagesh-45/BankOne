import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../config/api-config';
import {
  AccessDefinition,
  CreateRoleRequest,
  RoleSummary,
  UpdateRoleRequest
} from '../models/role';

@Injectable({
  providedIn: 'root'
})
export class RoleService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${API_BASE_URL}/roles`;

  listRoles(): Observable<RoleSummary[]> {
    return this.http.get<RoleSummary[]>(this.baseUrl);
  }

  getAccessCatalog(): Observable<AccessDefinition[]> {
    return this.http.get<AccessDefinition[]>(`${this.baseUrl}/access-catalog`);
  }

  createRole(request: CreateRoleRequest): Observable<RoleSummary> {
    return this.http.post<RoleSummary>(this.baseUrl, request);
  }

  updateRole(roleId: number, request: UpdateRoleRequest): Observable<RoleSummary> {
    return this.http.put<RoleSummary>(`${this.baseUrl}/${roleId}`, request);
  }
}
