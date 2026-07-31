import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';

import { Auth } from '../../../core/services/auth';
import { Notification } from '../../../core/services/notification';
import { BrandLogo } from '../../../shared/components/brand-logo/brand-logo';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-portal-layout',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
    BrandLogo
  ],
  templateUrl: './portal-layout.html',
  styleUrl: './portal-layout.scss'
})
export class PortalLayout {
  private readonly auth = inject(Auth);
  private readonly notification = inject(Notification);

  readonly appVersion = environment.appVersion;
  readonly displayName = this.auth.getDisplayName();

  logout(): void {
    this.auth.logout();
    this.notification.info('You have been logged out');
  }
}
