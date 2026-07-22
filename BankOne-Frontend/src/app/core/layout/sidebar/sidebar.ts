import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';

import { Auth } from '../../services/auth';
import { BrandLogo } from '../../../shared/components/brand-logo/brand-logo';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [
    RouterLink,
    RouterLinkActive,
    MatListModule,
    MatIconModule,
    BrandLogo
  ],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss'
})
export class Sidebar {
  private readonly auth = inject(Auth);

  readonly canAccessCustomers = this.auth.hasAnyRole(['ADMIN', 'EMPLOYEE', 'MANAGER']);
  readonly canAccessEmployees = this.auth.hasAnyRole(['ADMIN']);
}
