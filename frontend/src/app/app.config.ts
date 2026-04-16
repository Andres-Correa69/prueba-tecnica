import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { MAT_FORM_FIELD_DEFAULT_OPTIONS } from '@angular/material/form-field';

import { APP_ROUTES } from './app.routes';
import { authInterceptor } from './core/auth/auth.interceptor';

/**
 * Configuración raíz de la aplicación en modo standalone.
 *
 * Reemplaza al antiguo `AppModule` de Angular y al `<AuthProvider>`
 * que la versión React exponía en `main.tsx`. Cada `provide*` aquí
 * es el equivalente a un provider global:
 *
 *   - `provideRouter(APP_ROUTES)` ↔ `<BrowserRouter><Routes>…` de React.
 *   - `provideHttpClient(withInterceptors([...]))` ↔ los interceptores
 *     de `axiosCars` (Bearer + 401). Al registrarse aquí, cualquier
 *     `HttpClient` que se inyecte en toda la app hereda ese comportamiento.
 *   - `provideAnimationsAsync()` es requisito de Angular Material.
 *   - `MAT_FORM_FIELD_DEFAULT_OPTIONS` equivale al `defaultProps` de
 *     `MuiTextField` en el tema MUI: por defecto los `mat-form-field`
 *     serán `outline`, como pediamos en la versión React.
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(APP_ROUTES, withComponentInputBinding()),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideAnimationsAsync(),
    {
      provide: MAT_FORM_FIELD_DEFAULT_OPTIONS,
      useValue: { appearance: 'outline', subscriptSizing: 'dynamic' },
    },
  ],
};
