import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';

import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatCardModule, 
    MatFormFieldModule, MatInputModule, MatButtonModule, 
    MatProgressSpinnerModule, MatIconModule, MatSnackBarModule
  ],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  loginForm: FormGroup;
  isLoading = false;
  hidePassword = true;

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.loginForm.valid) {
      this.isLoading = true;

      this.http.post<any>('/api/v1/auth/authenticate', this.loginForm.value)
        .subscribe({
          next: (response) => {
            this.authService.saveToken(response.token, response.role);
            if (response.refreshToken) {
              this.authService.saveRefreshToken(response.refreshToken);
            }
            // Display Green Success Toast
            this.snackBar.open('Authentication Successful! Welcome Back.', 'Close', {
              duration: 3000, 
              panelClass: ['bg-secondary', 'text-white']
            });
            this.router.navigate(['/dashboard']);
          },
          error: (err) => {
            this.isLoading = false;
            let errorMessage = 'System Error: Connection to backend failed.';
            if (err.error && err.error.message) {
                 errorMessage = err.error.message;
            } else if (err.status === 401) {
                 errorMessage = 'Invalid Credentials. Please try again.';
            }

            // Display Red Error Toast
            this.snackBar.open(errorMessage, 'Close', {
              duration: 5000, 
              panelClass: ['bg-red-500', 'text-white']
            });
          }
        });
    }
  }
}
