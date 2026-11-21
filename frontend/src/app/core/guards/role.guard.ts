import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/** Allows only vets (`'vet'`) or only breeders (`'breeder'`); others go to their own home. */
export const roleGuard = (role: 'vet' | 'breeder'): CanActivateFn => () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }
  const isVet = auth.isVet();
  if ((role === 'vet') === isVet) {
    return true;
  }
  return router.createUrlTree([isVet ? '/vet-console' : '/dispatch']);
};
