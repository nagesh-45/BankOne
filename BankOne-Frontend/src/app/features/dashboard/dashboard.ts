import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { catchError, of } from 'rxjs';
import { StatCard } from '../../shared/components/stat-card/stat-card';

import { Auth } from '../../core/services/auth';
import { DashboardService } from '../../core/services/dashboard.service';
import { DashboardSummary } from '../../core/models/dashboard-summary';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    MatCardModule,
    StatCard
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard {

  private readonly dashboardService = inject(DashboardService);
  private readonly auth = inject(Auth);

  readonly canAccessCustomers = this.auth.hasAnyRole(['ADMIN', 'EMPLOYEE', 'MANAGER']);
  readonly canAccessEmployees = this.auth.hasAnyRole(['ADMIN']);

  readonly summary = toSignal(
    this.dashboardService.getDashboardSummary().pipe(
      catchError((error) => {
        console.error('Failed to load dashboard summary', error);
        return of(null);
      })
    ),
    { initialValue: null as DashboardSummary | null }
  );
}
