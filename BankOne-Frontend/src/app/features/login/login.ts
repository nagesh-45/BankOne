import { Notification } from '../../core/services/notification';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

import { Auth } from '../../core/services/auth';
import { LoginRequest } from '../../core/models/login-request';

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
    MatSnackBarModule
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})

export class Login {

  username = '';
  password = '';
  hidePassword = true;
  rememberMe = false;

  constructor(
    private auth: Auth,
    private router: Router,
    private notification: Notification
  ) {
  }

  login(): void {

    const request: LoginRequest = {
      username: this.username,
      password: this.password
    };

    this.auth.login(request).subscribe({
      next: (response: any) => {
        localStorage.setItem('accessToken', response.accessToken);
        localStorage.setItem('tokenType', response.tokenType);

        this.notification.success('Login successful');

        this.router.navigate(['/app/dashboard']);
      },
      error: (error: any) => {

        this.notification.error(
          error.error?.message ?? 'Login failed'
        );

      }
    });

  }

  test(): void {
    console.log('Button clicked');
  }
}
