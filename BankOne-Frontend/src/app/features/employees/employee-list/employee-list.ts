import { Component, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
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
import { Auth } from '../../../core/services/auth';
import { UserService } from '../../../core/services/user';
import { ListPagination } from '../../../shared/components/list-pagination/list-pagination';
import { LoadingState } from '../../../shared/components/loading-state/loading-state';
import { BusinessIdPipe } from '../../../core/pipes/business-id.pipe';
import { UserEditDialog } from '../user-edit-dialog/user-edit-dialog';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    ListPagination,
    LoadingState,
    BusinessIdPipe
  ],
  templateUrl: './employee-list.html',
  styleUrl: './employee-list.scss'
})
export class EmployeeList {
  private readonly auth = inject(Auth);
  private readonly userService = inject(UserService);
  private readonly route = inject(ActivatedRoute);
  private readonly dialog = inject(MatDialog);

  readonly canEditEmployee = this.auth.hasAnyRole(['ADMIN']);

  private readonly initialQuery =
    this.route.snapshot.queryParamMap.get('q')?.trim() ?? '';

  readonly searchTerm = signal(this.initialQuery);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);
  readonly sortBy = signal('userId');
  readonly sortDir = signal<'asc' | 'desc'>('desc');
  readonly highlightCode = signal(
    this.initialQuery ? this.initialQuery.toUpperCase() : ''
  );
  private readonly reloadTick = signal(0);

  private readonly employeeResponse = toSignal(
    combineLatest([
      toObservable(this.searchTerm).pipe(
        debounceTime(250),
        distinctUntilChanged()
      ),
      toObservable(this.pageIndex),
      toObservable(this.pageSize),
      toObservable(this.sortBy),
      toObservable(this.sortDir),
      toObservable(this.reloadTick)
    ]).pipe(
      switchMap(([search, page, size, sortBy, sortDir]) =>
        this.userService.getEmployees(search.trim(), page, size, sortBy, sortDir).pipe(
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
    this.highlightCode.set('');
    this.searchTerm.set(value);
  }

  toggleSort(column: string): void {
    if (this.sortBy() === column) {
      this.sortDir.update((dir) => (dir === 'asc' ? 'desc' : 'asc'));
    } else {
      this.sortBy.set(column);
      this.sortDir.set('asc');
    }
    this.pageIndex.set(0);
  }

  sortIcon(column: string): string {
    if (this.sortBy() !== column) {
      return 'unfold_more';
    }
    return this.sortDir() === 'asc' ? 'arrow_upward' : 'arrow_downward';
  }

  changePage(page: number): void {
    this.pageIndex.set(page);
  }

  changePageSize(size: number): void {
    this.pageSize.set(size);
    this.pageIndex.set(0);
  }

  isHighlighted(employee: AppUser): boolean {
    const code = this.highlightCode();
    if (!code) {
      return false;
    }
    const employeeCode = (employee.employeeCode ?? `E${String(employee.userId).padStart(5, '0')}`)
      .toUpperCase();
    return employeeCode === code || String(employee.userId) === code;
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

  editEmployee(employee: AppUser, event: Event): void {
    event.stopPropagation();

    const dialogRef = this.dialog.open(UserEditDialog, {
      width: '640px',
      maxWidth: '95vw',
      disableClose: true,
      data: { employee }
    });

    dialogRef.afterClosed().subscribe((updated) => {
      if (updated) {
        this.reloadTick.update((value) => value + 1);
      }
    });
  }
}
