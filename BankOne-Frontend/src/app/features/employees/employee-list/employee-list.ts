import { Component, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import {
  catchError,
  combineLatest,
  debounceTime,
  distinctUntilChanged,
  map,
  of,
  startWith,
  switchMap
} from 'rxjs';

import { UserService } from '../../../core/services/user';
import { UserCreateDialog } from '../user-create-dialog/user-create-dialog';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule
  ],
  templateUrl: './employee-list.html',
  styleUrl: './employee-list.scss'
})
export class EmployeeList {
  private readonly userService = inject(UserService);
  private readonly dialog = inject(MatDialog);

  readonly searchTerm = signal('');
  private readonly reloadTick = signal(0);

  private readonly employeeResponse = toSignal(
    combineLatest([
      toObservable(this.searchTerm).pipe(
        debounceTime(250),
        distinctUntilChanged()
      ),
      toObservable(this.reloadTick)
    ]).pipe(
      switchMap(([search]) =>
        this.userService.getEmployees(search.trim()).pipe(
          map((employees) => ({
            state: 'loaded' as const,
            employees
          })),
          startWith({
            state: 'loading' as const,
            employees: []
          }),
          catchError((error) => {
            console.error('Failed to load employees', error);
            return of({
              state: 'error' as const,
              employees: []
            });
          })
        )
      )
    ),
    {
      initialValue: {
        state: 'loading' as const,
        employees: []
      }
    }
  );

  readonly employees = computed(() => this.employeeResponse().employees);
  readonly totalEmployees = computed(() => this.employees().length);
  readonly isLoading = computed(() => this.employeeResponse().state === 'loading');
  readonly hasError = computed(() => this.employeeResponse().state === 'error');

  updateSearch(value: string): void {
    this.searchTerm.set(value);
  }

  openCreateUser(): void {
    const dialogRef = this.dialog.open(UserCreateDialog, {
      width: '640px',
      maxWidth: '95vw',
      disableClose: true
    });

    dialogRef.afterClosed().subscribe((created) => {
      if (created) {
        this.reloadTick.update((tick) => tick + 1);
      }
    });
  }

  accessLabel(roles: string[]): string {
    if (roles.includes('ADMIN')) {
      return 'Admin access';
    }

    if (roles.includes('EMPLOYEE') || roles.includes('MANAGER')) {
      return 'Normal access';
    }

    return roles.join(', ') || 'No role';
  }
}
