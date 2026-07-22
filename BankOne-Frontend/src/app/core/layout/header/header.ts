import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';

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

  readonly displayName = this.auth.getDisplayName();

  logout(): void {
    this.auth.logout();
    this.notification.info('You have been logged out');
  }

}
