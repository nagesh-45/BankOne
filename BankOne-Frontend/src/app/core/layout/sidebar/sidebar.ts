import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

import { Auth } from '../../services/auth';
import { BrandLogo } from '../../../shared/components/brand-logo/brand-logo';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [
    RouterLink,
    RouterLinkActive,
    MatButtonModule,
    MatListModule,
    MatIconModule,
    MatTooltipModule,
    BrandLogo
  ],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss'
})
export class Sidebar {
  private readonly auth = inject(Auth);

  @Input() expanded = false;
  @Output() expandedChange = new EventEmitter<boolean>();

  readonly canAccessCustomers = this.auth.hasAnyRole(['ADMIN', 'EMPLOYEE', 'MANAGER']);
  readonly canAccessEmployees = this.auth.hasAnyRole(['ADMIN']);
  readonly canAccessManagement = this.auth.hasAnyRole(['ADMIN', 'EMPLOYEE']);

  toggleExpanded(): void {
    this.expandedChange.emit(!this.expanded);
  }
}
