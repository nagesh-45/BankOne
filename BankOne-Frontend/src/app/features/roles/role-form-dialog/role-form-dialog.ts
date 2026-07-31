import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { finalize } from 'rxjs';

import { AccessDefinition, RoleSummary } from '../../../core/models/role';
import { Notification } from '../../../core/services/notification';
import { RoleService } from '../../../core/services/role';
import { apiErrorMessage } from '../../../core/utils/api-error-message';

export type RoleFormDialogData =
  | { mode: 'create'; catalog: AccessDefinition[] }
  | { mode: 'edit'; role: RoleSummary; catalog: AccessDefinition[] };

export type RoleFormDialogResult = { roleId: number };

@Component({
  selector: 'app-role-form-dialog',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule
  ],
  templateUrl: './role-form-dialog.html',
  styleUrl: './role-form-dialog.scss'
})
export class RoleFormDialog {
  private readonly dialogRef =
    inject(MatDialogRef<RoleFormDialog, RoleFormDialogResult | false>);
  private readonly data = inject<RoleFormDialogData>(MAT_DIALOG_DATA);
  private readonly roleService = inject(RoleService);
  private readonly notification = inject(Notification);

  readonly mode = this.data.mode;
  readonly catalog = this.data.catalog;
  readonly saving = signal(false);

  roleName = this.data.mode === 'edit' ? this.data.role.roleName : '';
  description =
    this.data.mode === 'edit' ? (this.data.role.description ?? '') : '';
  selected = new Set(
    this.data.mode === 'edit' ? this.data.role.accessCodes : []
  );

  close(): void {
    this.dialogRef.close(false);
  }

  isChecked(code: string): boolean {
    return this.selected.has(code);
  }

  toggle(code: string, checked: boolean): void {
    if (checked) {
      this.selected.add(code);
    } else {
      this.selected.delete(code);
    }
  }

  save(): void {
    if (this.saving()) {
      return;
    }

    const accessCodes = [...this.selected].sort();

    if (this.mode === 'create') {
      const name = this.roleName.trim().toUpperCase().replace(/\s+/g, '_');
      if (!name) {
        this.notification.error('Role name is required');
        return;
      }

      this.saving.set(true);
      this.roleService.createRole({
        roleName: name,
        description: this.description.trim() || undefined,
        accessCodes
      }).pipe(
        finalize(() => this.saving.set(false))
      ).subscribe({
        next: (role) => {
          this.notification.success(`Role ${role.roleName} created`);
          this.dialogRef.close({ roleId: role.roleId });
        },
        error: (error) => {
          this.notification.error(apiErrorMessage(error, 'Failed to create role'));
        }
      });
      return;
    }

    const role = this.data.mode === 'edit' ? this.data.role : null;
    if (!role) {
      return;
    }

    this.saving.set(true);
    this.roleService.updateRole(role.roleId, {
      description: this.description.trim() || null,
      accessCodes
    }).pipe(
      finalize(() => this.saving.set(false))
    ).subscribe({
      next: (updated) => {
        this.notification.success(`Role ${updated.roleName} updated`);
        this.dialogRef.close({ roleId: updated.roleId });
      },
      error: (error) => {
        this.notification.error(apiErrorMessage(error, 'Failed to update role'));
      }
    });
  }
}
