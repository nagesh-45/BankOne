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

import { AppUser } from '../../../core/models/app-user';
import { PagedResponse } from '../../../core/models/paged-response';
import { UserService } from '../../../core/services/user';
import { ListPagination } from '../../../shared/components/list-pagination/list-pagination';
import { BusinessIdPipe } from '../../../core/pipes/business-id.pipe';
import { UserCreateDialog } from '../user-create-dialog/user-create-dialog';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    ListPagination,
    BusinessIdPipe
  ],
  templateUrl: './employee-list.html',
  styleUrl: './employee-list.scss'
})
export class EmployeeList {
  private readonly userService = inject(UserService);
  private readonly dialog = inject(MatDialog);

  readonly searchTerm = signal('');
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);
  private readonly reloadTick = signal(0);

  private readonly employeeResponse = toSignal(
    combineLatest([
      toObservable(this.searchTerm).pipe(
        debounceTime(250),
        distinctUntilChanged()
      ),
      toObservable(this.pageIndex),
      toObservable(this.pageSize),
      toObservable(this.reloadTick)
    ]).pipe(
      switchMap(([search, page, size]) =>
        this.userService.getEmployees(search.trim(), page, size).pipe(
          map((response) => ({
            state: 'loaded' as const,
            response
          })),
          startWith({
            state: 'loading' as const,
            response: null as PagedResponse<AppUser> | null
          }),
          catchError((error) => {
            console.error('Failed to load employees', error);
            return of({
              state: 'error' as const,
              response: null as PagedResponse<AppUser> | null
            });
          })
        )
      )
    ),
    {
      initialValue: {
        state: 'loading' as const,
        response: null as PagedResponse<AppUser> | null
      }
    }
  );

  readonly employees = computed(() => this.employeeResponse().response?.content ?? []);
  readonly totalEmployees = computed(
    () => this.employeeResponse().response?.totalElements ?? 0
  );
  readonly totalPages = computed(
    () => this.employeeResponse().response?.totalPages ?? 0
  );
  readonly isLoading = computed(() => this.employeeResponse().state === 'loading');
  readonly hasError = computed(() => this.employeeResponse().state === 'error');

  updateSearch(value: string): void {
    this.pageIndex.set(0);
    this.searchTerm.set(value);
  }

  changePage(page: number): void {
    this.pageIndex.set(page);
  }

  changePageSize(size: number): void {
    this.pageSize.set(size);
    this.pageIndex.set(0);
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
