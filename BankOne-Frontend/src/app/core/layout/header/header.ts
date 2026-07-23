import { Component, DestroyRef, Input, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { interval, map, startWith } from 'rxjs';

import { Auth } from '../../services/auth';
import { Notification } from '../../services/notification';
import { BrandLogo } from '../../../shared/components/brand-logo/brand-logo';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    RouterLink,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    BrandLogo
  ],
  templateUrl: './header.html',
  styleUrl: './header.scss'
})
export class Header {
  private readonly auth = inject(Auth);
  private readonly notification = inject(Notification);
  private readonly destroyRef = inject(DestroyRef);

  @Input() sidebarExpanded = false;

  readonly displayName = this.auth.getDisplayName();

  private readonly dateTimeFormatter = new Intl.DateTimeFormat(undefined, {
    weekday: 'short',
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: true,
    timeZoneName: 'short'
  });

  readonly systemTime = signal(this.formatNow());

  constructor() {
    interval(1000)
      .pipe(
        startWith(0),
        map(() => this.formatNow()),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((value) => this.systemTime.set(value));
  }

  logout(): void {
    this.auth.logout();
    this.notification.info('You have been logged out');
  }

  private formatNow(): string {
    return this.dateTimeFormatter.format(new Date());
  }
}
