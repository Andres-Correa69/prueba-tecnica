import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { environment } from '../../../environments/environment';
import { TokenStorageService } from './token-storage.service';

/**
 * Interceptor HTTP global. Reemplaza a los dos interceptores que vivían
 * sobre la instancia `axiosCars` en la versión React:
 *
 *   1. **Request**: si la URL apunta al `cars-service` y hay un token
 *      guardado, añade la cabecera `Authorization: Bearer <token>`.
 *      Deliberadamente NO lo añadimos al `auth-service` — los endpoints
 *      `POST /auth/register` y `POST /auth/login` son públicos y enviar
 *      un JWT (p. ej. el del registro anterior) sólo añadiría ruido al
 *      log del servidor.
 *
 *   2. **Response**: al recibir un 401 limpiamos el token y navegamos a
 *      `/login`. Esto cubre el caso "el token expiró en el servidor
 *      entre el último render y la siguiente petición".
 *
 * <p>Al registrarse con `provideHttpClient(withInterceptors([...]))` en
 * `app.config.ts`, TODA llamada hecha con `HttpClient` en la aplicación
 * pasa por aquí — sin necesidad de wrappers por servicio.</p>
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const storage = inject(TokenStorageService);
  const router = inject(Router);

  const isCarsRequest = req.url.startsWith(environment.carsUrl);
  const stored = storage.load();

  const authed = isCarsRequest && stored?.token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${stored.token}` } })
    : req;

  return next(authed).pipe(
    catchError((err) => {
      if (err?.status === 401 && isCarsRequest) {
        storage.clear();
        // `replaceUrl: true` evita que el back del navegador vuelva a
        // la URL protegida — igual que `window.location.replace` en la
        // versión React.
        if (router.url !== '/login') {
          router.navigate(['/login'], { replaceUrl: true });
        }
      }
      return throwError(() => err);
    }),
  );
};
