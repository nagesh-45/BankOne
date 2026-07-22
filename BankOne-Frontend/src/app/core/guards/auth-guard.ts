import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { Auth } from '../services/auth';

export const authGuard: CanActivateFn = (route) => {
  const auth = inject(Auth);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    router.navigate(['/']);
    return false;
  }

  const allowedRoles = route.data?.['roles'] as string[] | undefined;

  if (!allowedRoles || auth.hasAnyRole(allowedRoles)) {
    return true;
  }

  router.navigate(['/app/dashboard']);
  return false;
};
