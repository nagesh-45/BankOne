import { CurrencyPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { catchError, of } from 'rxjs';

import { Account } from '../../../core/models/account';
import { PortalService } from '../../../core/services/portal';
import { LoadingState } from '../../../shared/components/loading-state/loading-state';
import {
  PortalTransferDialog,
  PortalTransferDialogData
} from '../portal-transfer-dialog/portal-transfer-dialog';

@Component({
  selector: 'app-my-accounts',
  standalone: true,
  imports: [
    CurrencyPipe,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    LoadingState
  ],
  templateUrl: './my-accounts.html',
  styleUrl: './my-accounts.scss'
})
export class MyAccounts {
  private readonly portal = inject(PortalService);
  private readonly dialog = inject(MatDialog);

  readonly loading = signal(true);
  readonly error = signal(false);
  readonly accounts = signal<Account[]>([]);

  constructor() {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(false);
    this.portal.getMyAccounts().pipe(
      catchError(() => {
        this.error.set(true);
        return of([] as Account[]);
      })
    ).subscribe((accounts) => {
      this.accounts.set(accounts);
      this.loading.set(false);
    });
  }

  openTransfer(fromAccountId?: number): void {
    const accounts = this.accounts();
    if (accounts.length === 0) {
      return;
    }
    this.dialog.open<PortalTransferDialog, PortalTransferDialogData, boolean>(
      PortalTransferDialog,
      {
        width: '520px',
        maxWidth: '95vw',
        disableClose: true,
        data: { accounts, fromAccountId }
      }
    ).afterClosed().subscribe((ok) => {
      if (ok) {
        this.reload();
      }
    });
  }
}
