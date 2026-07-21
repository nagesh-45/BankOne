import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';

import { Auth } from '../../core/services/auth';
import { LoginRequest } from '../../core/models/login-request';

@Component({
  selector: 'app-login',
  imports: [
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {

  username = '';
  password = '';
  hidePassword = true;
  rememberMe = false;

  constructor(private auth: Auth) {
  }

  login(): void {

    const request: LoginRequest = {
      username: this.username,
      password: this.password
    };

    this.auth.login(request).subscribe({
      next: (response) => {
        console.log('Login Success');
        console.log(response);
      },
      error: (error) => {
        console.error('Login Failed');
        console.error(error);
      }
    });

  }
  test(): void {
    console.log('Button clicked');
  }
}
