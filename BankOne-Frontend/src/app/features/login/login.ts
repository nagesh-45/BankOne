import { Notification } from '../../core/services/notification';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';

import { Auth } from '../../core/services/auth';
import { LoginRequest } from '../../core/models/login-request';
import { BrandLogo } from '../../shared/components/brand-logo/brand-logo';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSnackBarModule,
    BrandLogo
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  username = '';
  password = '';
  hidePassword = true;
  rememberMe = false;
  readonly loading = signal(false);

  constructor(
    private auth: Auth,
    private router: Router,
    private notification: Notification
  ) {
    this.rememberMe = this.auth.wasRememberMeSelected();
    this.username = this.auth.getRememberedUsername();
  }

  login(): void {
    const username = this.username.trim();
    const password = this.password.trim();

    if (!username || !password) {
      this.notification.error('Username and password are required');
      return;
    }

    this.loading.set(true);
    this.auth.clearSession();

    const request: LoginRequest = {
      username,
      password
    };

    this.auth.login(request).pipe(
      finalize(() => this.loading.set(false))
    ).subscribe({
      next: (response) => {
        this.auth.saveSession(response, this.rememberMe);
        this.notification.success('Login successful');
        this.router.navigate(['/app/dashboard']);
      },
      error: (error) => {
        this.notification.error(
          error.error?.message ?? 'Login failed'
        );
      }
    });
  }
}
