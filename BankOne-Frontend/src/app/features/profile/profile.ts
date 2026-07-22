import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { catchError, map, of, startWith } from 'rxjs';

import { Auth } from '../../core/services/auth';
import { UserProfile } from '../../core/models/user-profile';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.scss'
})
export class Profile {
  private readonly auth = inject(Auth);

  private readonly profileResponse = toSignal(
    this.auth.getProfile().pipe(
      map((profile) => ({ state: 'loaded' as const, profile })),
      startWith({ state: 'loading' as const, profile: null as UserProfile | null }),
      catchError((error) => {
        console.error('Failed to load profile', error);
        return of({ state: 'error' as const, profile: null as UserProfile | null });
      })
    ),
    {
      initialValue: {
        state: 'loading' as const,
        profile: null as UserProfile | null
      }
    }
  );

  readonly isLoading = computed(() => this.profileResponse().state === 'loading');
  readonly hasError = computed(() => this.profileResponse().state === 'error');
  readonly profile = computed(() => this.profileResponse().profile);

  fullName(profile: UserProfile): string {
    return `${profile.firstName ?? ''} ${profile.lastName ?? ''}`.trim() || profile.username;
  }

  roleLabel(roles: string[]): string {
    if (roles.includes('ADMIN')) {
      return 'Administrator';
    }
    if (roles.includes('MANAGER')) {
      return 'Manager';
    }
    if (roles.includes('EMPLOYEE')) {
      return 'Employee';
    }
    return roles.join(', ') || 'No role assigned';
  }

  formatDate(value: string | null): string {
    if (!value) {
      return 'Never';
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return value;
    }

    return date.toLocaleString();
  }
}
