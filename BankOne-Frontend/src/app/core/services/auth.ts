import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';

import { LoginRequest } from '../models/login-request';
import { LoginResponse } from '../models/login-response';
import { UserProfile } from '../models/user-profile';
import { DashboardService } from './dashboard.service';

@Injectable({
  providedIn: 'root'
})
export class Auth {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly dashboardService = inject(DashboardService);

  private readonly baseUrl = 'http://localhost:8080/auth';
  private readonly accessTokenKey = 'accessToken';
  private readonly tokenTypeKey = 'tokenType';
  private readonly rolesKey = 'roles';
  private readonly usernameKey = 'username';
  private readonly firstNameKey = 'firstName';
  private readonly lastNameKey = 'lastName';
  private readonly emailKey = 'email';

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(
      `${this.baseUrl}/login`,
      request
    );
  }

  getProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.baseUrl}/me`);
  }

  changePassword(request: {
    currentPassword: string;
    newPassword: string;
    confirmPassword: string;
  }): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/password`, request);
  }

  saveSession(response: LoginResponse): void {
    localStorage.setItem(this.accessTokenKey, response.accessToken);
    localStorage.setItem(this.tokenTypeKey, response.tokenType);
    localStorage.setItem(this.rolesKey, JSON.stringify(response.roles ?? []));
    localStorage.setItem(this.usernameKey, response.username ?? '');
    localStorage.setItem(this.firstNameKey, response.firstName ?? '');
    localStorage.setItem(this.lastNameKey, response.lastName ?? '');
    localStorage.setItem(this.emailKey, response.email ?? '');
  }

  clearSession(): void {
    localStorage.removeItem(this.accessTokenKey);
    localStorage.removeItem(this.tokenTypeKey);
    localStorage.removeItem(this.rolesKey);
    localStorage.removeItem(this.usernameKey);
    localStorage.removeItem(this.firstNameKey);
    localStorage.removeItem(this.lastNameKey);
    localStorage.removeItem(this.emailKey);
  }

  getAccessToken(): string | null {
    return localStorage.getItem(this.accessTokenKey);
  }

  isAuthenticated(): boolean {
    return !!this.getAccessToken();
  }

  getUsername(): string {
    const stored = localStorage.getItem(this.usernameKey);
    if (stored) {
      return stored;
    }

    return this.usernameFromToken();
  }

  getDisplayName(): string {
    const firstName = localStorage.getItem(this.firstNameKey)?.trim() ?? '';
    const lastName = localStorage.getItem(this.lastNameKey)?.trim() ?? '';
    const fullName = `${firstName} ${lastName}`.trim();

    if (fullName) {
      return fullName;
    }

    return this.getUsername() || 'User';
  }

  private usernameFromToken(): string {
    const token = this.getAccessToken();
    if (!token) {
      return '';
    }

    try {
      const payload = JSON.parse(atob(token.split('.')[1] ?? '')) as {
        sub?: string;
        username?: string;
      };
      return payload.sub ?? payload.username ?? '';
    } catch {
      return '';
    }
  }

  getRoles(): string[] {
    const roles = localStorage.getItem(this.rolesKey);

    if (!roles) {
      return [];
    }

    try {
      return JSON.parse(roles) as string[];
    } catch {
      return [];
    }
  }

  hasAnyRole(allowedRoles: string[]): boolean {
    const userRoles = this.getRoles();
    return allowedRoles.some((role) => userRoles.includes(role));
  }

  logout(): void {
    this.clearSession();
    this.dashboardService.clearCache();
    this.router.navigate(['/']);
  }

}
