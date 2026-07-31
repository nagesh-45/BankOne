import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

import { Auth } from '../../services/auth';
import { BrandLogo } from '../../../shared/components/brand-logo/brand-logo';
import { environment } from '../../../../environments/environment';

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

  readonly appVersion = environment.appVersion;

  @Input() expanded = false;
  @Output() expandedChange = new EventEmitter<boolean>();

  readonly canAccessCustomers = this.auth.can(
    ['CUSTOMERS_READ'],
    ['ADMIN', 'EMPLOYEE', 'MANAGER']
  );
  readonly canAccessAccounts = this.auth.can(
    ['ACCOUNTS_READ'],
    ['ADMIN', 'EMPLOYEE', 'MANAGER', 'TELLER', 'AUDITOR']
  );
  readonly canAccessTransactions = this.auth.can(
    ['ACCOUNTS_READ'],
    ['ADMIN', 'EMPLOYEE', 'MANAGER', 'TELLER', 'AUDITOR']
  );
  readonly canAccessReports = this.auth.can(
    ['ACCOUNTS_READ', 'DASHBOARD'],
    ['ADMIN', 'EMPLOYEE', 'MANAGER', 'TELLER', 'AUDITOR']
  );
  readonly canAccessEmployees = this.auth.can(['USERS_MANAGE'], ['ADMIN']);
  readonly canAccessRoles = this.auth.can(['ROLES_MANAGE'], ['ADMIN']);
  readonly canAccessTransferApprovals = this.auth.can(
    ['ACCOUNTS_WRITE'],
    ['ADMIN', 'EMPLOYEE', 'MANAGER']
  );
  readonly canAccessAudit = this.auth.hasAnyRole(['ADMIN', 'MANAGER', 'AUDITOR']);
  readonly canAccessManagement = this.auth.can(
    [
      'CUSTOMERS_WRITE',
      'USERS_MANAGE',
      'ROLES_MANAGE',
      'POLICIES_MANAGE',
      'ACCOUNTS_READ',
      'ACCOUNTS_WRITE'
    ],
    ['ADMIN', 'EMPLOYEE', 'MANAGER', 'TELLER', 'AUDITOR']
  );

  toggleExpanded(): void {
    this.expandedChange.emit(!this.expanded);
  }
}
