import { Component, inject, OnInit } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { StatCard } from '../../shared/components/stat-card/stat-card';

import { Auth } from '../../core/services/auth';
import { DashboardService } from '../../core/services/dashboard.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    MatButtonModule,
    MatIconModule,
    StatCard
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {
  private readonly dashboardService = inject(DashboardService);
  private readonly auth = inject(Auth);

  readonly canAccessCustomers = this.auth.hasAnyRole(['ADMIN', 'EMPLOYEE', 'MANAGER']);
  readonly canAccessEmployees = this.auth.hasAnyRole(['ADMIN']);

  readonly summary = toSignal(this.dashboardService.summary$, { initialValue: null });
  readonly isLoading = toSignal(this.dashboardService.loading$, { initialValue: false });
  readonly hasError = toSignal(this.dashboardService.error$, { initialValue: false });

  ngOnInit(): void {
    this.dashboardService.ensureLoaded();
  }

  refresh(): void {
    this.dashboardService.refresh();
  }
}
