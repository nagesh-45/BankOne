import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../config/api-config';
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

  private readonly baseUrl = `${API_BASE_URL}/auth`;
  private readonly accessTokenKey = 'accessToken';
  private readonly tokenTypeKey = 'tokenType';
  private readonly rolesKey = 'roles';
  private readonly usernameKey = 'username';
  private readonly firstNameKey = 'firstName';
  private readonly lastNameKey = 'lastName';
  private readonly emailKey = 'email';
  private readonly rememberMeKey = 'rememberMe';
  private readonly rememberedUsernameKey = 'rememberedUsername';

  private readonly sessionKeys = [
    this.accessTokenKey,
    this.tokenTypeKey,
    this.rolesKey,
    this.usernameKey,
    this.firstNameKey,
    this.lastNameKey,
    this.emailKey
  ] as const;

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

  saveSession(response: LoginResponse, rememberMe = false): void {
    this.clearSession();

    const storage = rememberMe ? localStorage : sessionStorage;
    storage.setItem(this.accessTokenKey, response.accessToken);
    storage.setItem(this.tokenTypeKey, response.tokenType);
    storage.setItem(this.rolesKey, JSON.stringify(response.roles ?? []));
    storage.setItem(this.usernameKey, response.username ?? '');
    storage.setItem(this.firstNameKey, response.firstName ?? '');
    storage.setItem(this.lastNameKey, response.lastName ?? '');
    storage.setItem(this.emailKey, response.email ?? '');

    if (rememberMe) {
      localStorage.setItem(this.rememberMeKey, 'true');
      localStorage.setItem(
        this.rememberedUsernameKey,
        response.username ?? ''
      );
    } else {
      localStorage.removeItem(this.rememberMeKey);
      localStorage.removeItem(this.rememberedUsernameKey);
    }
  }

  clearSession(): void {
    for (const key of this.sessionKeys) {
      localStorage.removeItem(key);
      sessionStorage.removeItem(key);
    }
  }

  getAccessToken(): string | null {
    return this.read(this.accessTokenKey);
  }

  isAuthenticated(): boolean {
    return !!this.getAccessToken();
  }

  wasRememberMeSelected(): boolean {
    return localStorage.getItem(this.rememberMeKey) === 'true';
  }

  getRememberedUsername(): string {
    return localStorage.getItem(this.rememberedUsernameKey) ?? '';
  }

  getUsername(): string {
    const stored = this.read(this.usernameKey);
    if (stored) {
      return stored;
    }

    return this.usernameFromToken();
  }

  getDisplayName(): string {
    const firstName = this.read(this.firstNameKey)?.trim() ?? '';
    const lastName = this.read(this.lastNameKey)?.trim() ?? '';
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
    const roles = this.read(this.rolesKey);

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

  private read(key: string): string | null {
    // Prefer session (non-remember) token, but ignore empty strings
    return sessionStorage.getItem(key) || localStorage.getItem(key) || null;
  }
}
