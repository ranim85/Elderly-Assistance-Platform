import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true; // Use is allowed to access the route
  } else {
    // Save the completely blocked URL to state (optional enterprise pattern)
    // Send them to login
    router.navigate(['/login']);
    return false;
  }
};
