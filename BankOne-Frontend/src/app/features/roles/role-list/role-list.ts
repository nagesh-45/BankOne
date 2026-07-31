import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import {
  catchError,
  finalize,
  forkJoin,
  of
} from 'rxjs';

import { AccessDefinition, RoleSummary } from '../../../core/models/role';
import { Notification } from '../../../core/services/notification';
import { RoleService } from '../../../core/services/role';
import { apiErrorMessage } from '../../../core/utils/api-error-message';
import { LoadingState } from '../../../shared/components/loading-state/loading-state';
import {
  RoleFormDialog,
  RoleFormDialogData,
  RoleFormDialogResult
} from '../role-form-dialog/role-form-dialog';

@Component({
  selector: 'app-role-list',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    LoadingState
  ],
  templateUrl: './role-list.html',
  styleUrl: './role-list.scss'
})
export class RoleList {
  private readonly roleService = inject(RoleService);
  private readonly notification = inject(Notification);
  private readonly dialog = inject(MatDialog);

  readonly loading = signal(true);
  readonly error = signal(false);
  readonly roles = signal<RoleSummary[]>([]);
  readonly catalog = signal<AccessDefinition[]>([]);
  readonly selectedRoleId = signal<number | null>(null);
  readonly search = signal('');

  readonly selectedRole = computed(() => {
    const id = this.selectedRoleId();
    return this.roles().find((role) => role.roleId === id) ?? null;
  });

  readonly filteredRoles = computed(() => {
    const q = this.search().trim().toLowerCase();
    const all = this.roles();
    if (!q) {
      return all;
    }
    return all.filter((role) =>
      role.roleName.toLowerCase().includes(q)
      || (role.description ?? '').toLowerCase().includes(q)
    );
  });

  constructor() {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(false);

    forkJoin({
      roles: this.roleService.listRoles(),
      catalog: this.roleService.getAccessCatalog()
    }).pipe(
      catchError(() => {
        this.error.set(true);
        return of({ roles: [] as RoleSummary[], catalog: [] as AccessDefinition[] });
      }),
      finalize(() => this.loading.set(false))
    ).subscribe(({ roles, catalog }) => {
      this.roles.set(roles);
      this.catalog.set(catalog);
      const selected = this.selectedRoleId();
      if (!selected || !roles.some((role) => role.roleId === selected)) {
        this.selectedRoleId.set(roles[0]?.roleId ?? null);
      }
    });
  }

  selectRole(roleId: number): void {
    this.selectedRoleId.set(roleId);
  }

  accessLabel(code: string): string {
    return this.catalog().find((item) => item.code === code)?.label ?? code;
  }

  openCreate(): void {
    this.openForm({ mode: 'create', catalog: this.catalog() });
  }

  openEdit(): void {
    const role = this.selectedRole();
    if (!role) {
      return;
    }
    this.openForm({ mode: 'edit', role, catalog: this.catalog() });
  }

  private openForm(data: RoleFormDialogData): void {
    this.dialog.open<RoleFormDialog, RoleFormDialogData, RoleFormDialogResult | false>(
      RoleFormDialog,
      {
        width: '640px',
        maxWidth: '95vw',
        disableClose: true,
        data
      }
    ).afterClosed().subscribe((result) => {
      if (result) {
        this.reload();
        this.selectedRoleId.set(result.roleId);
      }
    });
  }
}
