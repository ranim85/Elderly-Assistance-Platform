import {
  HttpClient,
  HttpContext,
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { SKIP_AUTH, SKIP_REFRESH_RETRY } from '../http-context';

interface AuthPairResponse {
  token: string;
  refreshToken: string;
  role: string;
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const http = inject(HttpClient);
  const router = inject(Router);

  const skipAuth = req.context.get(SKIP_AUTH);
  const skipRefresh = req.context.get(SKIP_REFRESH_RETRY);
  const isAuthApi = req.url.includes('/api/v1/auth/');

  let outbound = req;
  const token = authService.getToken();
  if (token && !skipAuth) {
    outbound = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  return next(outbound).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status !== 401) {
        return throwError(() => error);
      }
      if (isAuthApi || skipRefresh) {
        return throwError(() => error);
      }

      const refreshToken = authService.getRefreshToken();
      if (!refreshToken) {
        authService.logout();
        router.navigate(['/login']);
        return throwError(() => error);
      }

      const refreshCtx = new HttpContext().set(SKIP_AUTH, true).set(SKIP_REFRESH_RETRY, true);
      return http
        .post<AuthPairResponse>(
          '/api/v1/auth/refresh',
          { refreshToken },
          { context: refreshCtx }
        )
        .pipe(
          switchMap((res) => {
            authService.saveToken(res.token, res.role);
            authService.saveRefreshToken(res.refreshToken);
            const retry = req.clone({
              setHeaders: { Authorization: `Bearer ${res.token}` }
            });
            return next(retry);
          }),
          catchError(() => {
            authService.logout();
            router.navigate(['/login']);
            return throwError(() => error);
          })
        );
    })
  );
};
