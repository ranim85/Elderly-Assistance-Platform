import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.hasToken()) {
    return router.createUrlTree(['/login']);
  }

  const requiredRoles = route.data['roles'] as Array<string> | undefined;
  const userRole = authService.getRole();

  if (requiredRoles && userRole && requiredRoles.includes(userRole)) {
    return true;
  }

  return router.createUrlTree(['/dashboard']);
};
