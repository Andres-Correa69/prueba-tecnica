import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from './auth.service';

/**
 * Guard que bloquea `/cars` cuando no hay sesión.
 *
 * Es el equivalente al `<ProtectedRoute>` de la versión React: si
 * `isAuthenticated()` es falso, redirigimos a `/login` preservando la
 * URL original en `queryParams.returnUrl` para poder volver al usuario
 * exactamente donde estaba después de autenticarse.
 *
 * Usamos la forma funcional (`CanActivateFn`) — idiomática en Angular 15+
 * — en lugar de la clase `CanActivate` legacy.
 */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isAuthenticated()) return true;
  return router.createUrlTree(['/login'], {
    queryParams: { returnUrl: state.url },
  });
};
