import { Component, OnInit, inject } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { StatCard } from '../../shared/components/stat-card/stat-card';

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
export class Dashboard implements OnInit {

  private readonly dashboardService = inject(DashboardService);

  summary?: DashboardSummary;

  ngOnInit(): void {
    this.dashboardService.getDashboardSummary().subscribe({
      next: (response) => {
        console.log('Dashboard response:', response);
        console.log('Customer count:', response.customerCount);
        this.summary = response;
        console.log('Summary after assignment:', this.summary);
      },
      error: (error) => {
        console.error('Failed to load dashboard summary', error);
      }
    });
  }
}
