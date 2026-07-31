import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

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
  private readonly accessesKey = 'accesses';
  private readonly customerIdKey = 'customerId';
  private readonly portalUserKey = 'portalUser';
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
    this.accessesKey,
    this.customerIdKey,
    this.portalUserKey,
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
    storage.setItem(this.accessesKey, JSON.stringify(response.accesses ?? []));
    if (response.customerId != null) {
      storage.setItem(this.customerIdKey, String(response.customerId));
    }
    storage.setItem(
      this.portalUserKey,
      response.portalUser || this.hasAnyRole(['CUSTOMER']) ? 'true' : 'false'
    );
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

  getAccesses(): string[] {
    const accesses = this.read(this.accessesKey);

    if (!accesses) {
      return [];
    }

    try {
      return JSON.parse(accesses) as string[];
    } catch {
      return [];
    }
  }

  hasAnyRole(allowedRoles: string[]): boolean {
    const userRoles = this.getRoles();
    return allowedRoles.some((role) => userRoles.includes(role));
  }

  hasAnyAccess(allowedAccesses: string[]): boolean {
    const userAccesses = this.getAccesses();
    return allowedAccesses.some((access) => userAccesses.includes(access));
  }

  /** Prefer access codes when present; fall back to roles for older sessions. */
  can(accesses: string[], fallbackRoles: string[] = []): boolean {
    if (this.getAccesses().length > 0) {
      return this.hasAnyAccess(accesses);
    }
    return fallbackRoles.length === 0 || this.hasAnyRole(fallbackRoles);
  }

  isPortalUser(): boolean {
    if (this.read(this.portalUserKey) === 'true') {
      return true;
    }
    return this.hasAnyRole(['CUSTOMER']) || this.hasAnyAccess(['PORTAL_ACCOUNTS']);
  }

  /** Staff banking UI (dashboard, CRM, etc.). */
  isStaffUser(): boolean {
    const staffAccesses = [
      'DASHBOARD',
      'CUSTOMERS_READ',
      'CUSTOMERS_WRITE',
      'ACCOUNTS_READ',
      'ACCOUNTS_WRITE',
      'USERS_MANAGE',
      'ROLES_MANAGE',
      'POLICIES_MANAGE'
    ];
    if (this.getAccesses().length > 0) {
      return this.hasAnyAccess(staffAccesses);
    }
    return this.hasAnyRole(['ADMIN', 'EMPLOYEE', 'MANAGER', 'TELLER', 'AUDITOR']);
  }

  /** Home route after login. */
  homeRoute(): string {
    if (this.isPortalUser() && !this.isStaffUser()) {
      return '/portal/accounts';
    }
    return '/app/dashboard';
  }

  getCustomerId(): number | null {
    const raw = this.read(this.customerIdKey);
    if (!raw) {
      return null;
    }
    const id = Number(raw);
    return Number.isFinite(id) ? id : null;
  }

  logout(): void {
    this.http.post<void>(`${this.baseUrl}/logout`, {}).pipe(
      finalize(() => {
        this.clearSession();
        this.dashboardService.clearCache();
        this.router.navigate(['/']);
      })
    ).subscribe({ error: () => undefined });
  }

  private read(key: string): string | null {
    // Prefer session (non-remember) token, but ignore empty strings
    return sessionStorage.getItem(key) || localStorage.getItem(key) || null;
  }
}
