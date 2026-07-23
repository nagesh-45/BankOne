import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';

import { API_BASE_URL } from '../config/api-config';
import { DashboardSummary } from '../models/dashboard-summary';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${API_BASE_URL}/dashboard`;

  private readonly summarySubject = new BehaviorSubject<DashboardSummary | null>(null);
  private readonly loadingSubject = new BehaviorSubject<boolean>(false);
  private readonly errorSubject = new BehaviorSubject<boolean>(false);
  private hasLoaded = false;

  readonly summary$: Observable<DashboardSummary | null> = this.summarySubject.asObservable();
  readonly loading$: Observable<boolean> = this.loadingSubject.asObservable();
  readonly error$: Observable<boolean> = this.errorSubject.asObservable();

  /** Load once; later visits reuse cached data until refresh(). */
  ensureLoaded(): void {
    if (this.hasLoaded || this.loadingSubject.value) {
      return;
    }

    this.fetchSummary();
  }

  /** Force a fresh request from the API. */
  refresh(): void {
    this.fetchSummary();
  }

  clearCache(): void {
    this.hasLoaded = false;
    this.summarySubject.next(null);
    this.errorSubject.next(false);
  }

  private fetchSummary(): void {
    this.loadingSubject.next(true);
    this.errorSubject.next(false);

    this.http.get<DashboardSummary>(this.apiUrl).subscribe({
      next: (summary) => {
        this.summarySubject.next(summary);
        this.hasLoaded = true;
        this.loadingSubject.next(false);
      },
      error: (error) => {
        console.error('Failed to load dashboard summary', error);
        this.errorSubject.next(true);
        this.loadingSubject.next(false);
      }
    });
  }
}
