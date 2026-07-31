import { CurrencyPipe, DecimalPipe } from '@angular/common';
import {
  Component,
  ElementRef,
  Injector,
  OnDestroy,
  ViewChild,
  afterNextRender,
  inject,
  signal
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import {
  ArcElement,
  BarController,
  BarElement,
  CategoryScale,
  Chart,
  DoughnutController,
  Legend,
  LineController,
  LineElement,
  LinearScale,
  PieController,
  PointElement,
  Tooltip
} from 'chart.js';
import { finalize } from 'rxjs';

import {
  AccountMixReport,
  ApprovalsReport,
  ReportCategory,
  TransactionTrendsReport
} from '../../core/models/report';
import { Auth } from '../../core/services/auth';
import { Notification } from '../../core/services/notification';
import { ReportService } from '../../core/services/report';
import { apiErrorMessage } from '../../core/utils/api-error-message';
import { LoadingState } from '../../shared/components/loading-state/loading-state';

Chart.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  LineController,
  BarElement,
  BarController,
  ArcElement,
  DoughnutController,
  PieController,
  Tooltip,
  Legend
);

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [
    CurrencyPipe,
    DecimalPipe,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    LoadingState
  ],
  templateUrl: './reports.html',
  styleUrl: './reports.scss'
})
export class Reports implements OnDestroy {
  private readonly reportService = inject(ReportService);
  private readonly auth = inject(Auth);
  private readonly notification = inject(Notification);
  private readonly sanitizer = inject(DomSanitizer);
  private readonly injector = inject(Injector);

  @ViewChild('chartOne') chartOneRef?: ElementRef<HTMLCanvasElement>;
  @ViewChild('chartTwo') chartTwoRef?: ElementRef<HTMLCanvasElement>;

  readonly canApprovals = this.auth.hasAnyRole(['ADMIN', 'MANAGER', 'AUDITOR']);

  readonly selected = signal<ReportCategory | null>(null);
  readonly loading = signal(false);
  readonly generating = signal(false);
  readonly fromDate = signal(this.isoDaysAgo(29));
  readonly toDate = signal(this.isoDaysAgo(0));

  readonly trends = signal<TransactionTrendsReport | null>(null);
  readonly accountMix = signal<AccountMixReport | null>(null);
  readonly approvals = signal<ApprovalsReport | null>(null);

  readonly pdfUrl = signal<SafeResourceUrl | null>(null);
  readonly pdfBlobUrl = signal<string | null>(null);
  readonly pdfFileName = signal('report.pdf');

  private chartOne?: Chart;
  private chartTwo?: Chart;

  ngOnDestroy(): void {
    this.destroyCharts();
    this.revokePdf();
  }

  selectCategory(category: ReportCategory): void {
    if (category === 'approvals' && !this.canApprovals) {
      this.notification.error('Approvals report is limited to Admin, Manager, or Auditor');
      return;
    }
    this.selected.set(category);
    this.revokePdf();
    this.loadData();
  }

  reload(): void {
    if (!this.selected()) {
      return;
    }
    this.loadData();
  }

  generatePdf(): void {
    const category = this.selected();
    if (!category || this.generating()) {
      return;
    }

    this.generating.set(true);
    this.reportService
      .downloadPdf(category, this.fromDate(), this.toDate())
      .pipe(finalize(() => this.generating.set(false)))
      .subscribe({
        next: (blob) => {
          this.revokePdf();
          const url = URL.createObjectURL(blob);
          this.pdfBlobUrl.set(url);
          this.pdfUrl.set(this.sanitizer.bypassSecurityTrustResourceUrl(url));
          this.pdfFileName.set(`${category}.pdf`);
          this.notification.success('PDF generated');
        },
        error: (error) => {
          this.notification.error(apiErrorMessage(error, 'Failed to generate PDF'));
        }
      });
  }

  downloadPdf(): void {
    const url = this.pdfBlobUrl();
    if (!url) {
      return;
    }
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = this.pdfFileName();
    anchor.click();
  }

  private loadData(): void {
    const category = this.selected();
    if (!category) {
      return;
    }

    this.loading.set(true);
    this.trends.set(null);
    this.accountMix.set(null);
    this.approvals.set(null);
    this.destroyCharts();

    if (category === 'transaction-trends') {
      this.reportService.transactionTrends(this.fromDate(), this.toDate()).subscribe({
        next: (report) => {
          this.trends.set(report);
          this.loading.set(false);
          this.scheduleChartPaint(() => this.renderTrends(report));
        },
        error: (error) => {
          this.loading.set(false);
          this.notification.error(apiErrorMessage(error, 'Failed to load report'));
        }
      });
      return;
    }

    if (category === 'account-mix') {
      this.reportService.accountMix().subscribe({
        next: (report) => {
          this.accountMix.set(report);
          this.loading.set(false);
          this.scheduleChartPaint(() => this.renderAccountMix(report));
        },
        error: (error) => {
          this.loading.set(false);
          this.notification.error(apiErrorMessage(error, 'Failed to load report'));
        }
      });
      return;
    }

    this.reportService.approvals(this.fromDate(), this.toDate()).subscribe({
      next: (report) => {
        this.approvals.set(report);
        this.loading.set(false);
        this.scheduleChartPaint(() => this.renderApprovals(report));
      },
      error: (error) => {
        this.loading.set(false);
        this.notification.error(apiErrorMessage(error, 'Failed to load report'));
      }
    });
  }

  /** Wait until Angular paints canvases (they are inside @else of loading). */
  private scheduleChartPaint(paint: () => void): void {
    afterNextRender(
      () => {
        requestAnimationFrame(() => paint());
      },
      { injector: this.injector }
    );
  }

  private canvasPair(): [HTMLCanvasElement, HTMLCanvasElement] | null {
    const one = this.chartOneRef?.nativeElement;
    const two = this.chartTwoRef?.nativeElement;
    if (!one || !two) {
      return null;
    }
    return [one, two];
  }

  private renderTrends(report: TransactionTrendsReport): void {
    this.destroyCharts();
    const pair = this.canvasPair();
    if (!pair) {
      return;
    }
    const [one, two] = pair;

    this.chartOne = new Chart(one, {
      type: 'line',
      data: {
        labels: report.daily.map((d) => d.date),
        datasets: [
          {
            label: 'Credit amount',
            data: report.daily.map((d) => Number(d.creditAmount)),
            borderColor: '#166534',
            backgroundColor: 'rgba(22, 101, 52, 0.15)',
            tension: 0.25,
            fill: true
          },
          {
            label: 'Debit amount',
            data: report.daily.map((d) => Number(d.debitAmount)),
            borderColor: '#b91c1c',
            backgroundColor: 'rgba(185, 28, 28, 0.12)',
            tension: 0.25,
            fill: true
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { position: 'bottom' } }
      }
    });

    this.chartTwo = new Chart(two, {
      type: 'doughnut',
      data: {
        labels: ['Credit', 'Debit'],
        datasets: [{
          data: [
            Number(report.totalCreditAmount),
            Number(report.totalDebitAmount)
          ],
          backgroundColor: ['#22c55e', '#f87171']
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { position: 'bottom' } }
      }
    });
  }

  private renderAccountMix(report: AccountMixReport): void {
    this.destroyCharts();
    const pair = this.canvasPair();
    if (!pair) {
      return;
    }
    const [one, two] = pair;

    this.chartOne = new Chart(one, {
      type: 'pie',
      data: {
        labels: report.byType.map((x) => x.name),
        datasets: [{
          data: report.byType.map((x) => x.count),
          backgroundColor: ['#3b82f6', '#22c55e', '#f59e0b', '#a855f7', '#06b6d4', '#64748b']
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { position: 'bottom' } }
      }
    });

    this.chartTwo = new Chart(two, {
      type: 'pie',
      data: {
        labels: report.byStatus.map((x) => x.name),
        datasets: [{
          data: report.byStatus.map((x) => x.count),
          backgroundColor: ['#16a34a', '#dc2626', '#ca8a04', '#475569', '#7c3aed']
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { position: 'bottom' } }
      }
    });
  }

  private renderApprovals(report: ApprovalsReport): void {
    this.destroyCharts();
    const pair = this.canvasPair();
    if (!pair) {
      return;
    }
    const [one, two] = pair;

    this.chartOne = new Chart(one, {
      type: 'bar',
      data: {
        labels: report.byStatus.map((x) => x.name),
        datasets: [{
          label: 'Requests',
          data: report.byStatus.map((x) => x.count),
          backgroundColor: '#2563eb'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
      }
    });

    this.chartTwo = new Chart(two, {
      type: 'bar',
      data: {
        labels: report.byStaff.map((x) => x.name),
        datasets: [{
          label: 'Resolved',
          data: report.byStaff.map((x) => x.count),
          backgroundColor: '#0f766e'
        }]
      },
      options: {
        indexAxis: 'y',
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: { x: { beginAtZero: true, ticks: { precision: 0 } } }
      }
    });
  }

  private destroyCharts(): void {
    this.chartOne?.destroy();
    this.chartTwo?.destroy();
    this.chartOne = undefined;
    this.chartTwo = undefined;
  }

  private revokePdf(): void {
    const url = this.pdfBlobUrl();
    if (url) {
      URL.revokeObjectURL(url);
    }
    this.pdfBlobUrl.set(null);
    this.pdfUrl.set(null);
  }

  private isoDaysAgo(days: number): string {
    const d = new Date();
    d.setUTCDate(d.getUTCDate() - days);
    return d.toISOString().slice(0, 10);
  }
}
