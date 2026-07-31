import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { Auth } from '../services/auth';

/** Staff shell (/app/*) — bank customers are sent to the portal. */
export const staffAppGuard: CanActivateFn = () => {
  const auth = inject(Auth);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    router.navigate(['/']);
    return false;
  }

  if (auth.isPortalUser() && !auth.isStaffUser()) {
    router.navigate(['/portal/accounts']);
    return false;
  }

  return true;
};

/** Customer portal shell — staff without portal access stay in /app. */
export const portalGuard: CanActivateFn = () => {
  const auth = inject(Auth);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    router.navigate(['/']);
    return false;
  }

  if (!auth.isPortalUser() && auth.isStaffUser()) {
    router.navigate(['/app/dashboard']);
    return false;
  }

  if (!auth.can(['PORTAL_ACCOUNTS'], ['CUSTOMER'])) {
    router.navigate([auth.homeRoute()]);
    return false;
  }

  return true;
};
