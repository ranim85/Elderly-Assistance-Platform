import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { Router } from '@angular/router';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();

  // Clone the request and attach the JWT token if it exists
  let authReq = req;
  if (token) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  // Pass it to the next handler and intercept errors globally
  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // If our API returns 401 Unauthorized, the token is invalid/expired
      if (error.status === 401) {
        authService.logout();
        router.navigate(['/login']); // Force user back to login page
      }
      return throwError(() => error);
    })
  );
};
