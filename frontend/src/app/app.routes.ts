import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

/**
 * Tabla de rutas de la aplicación.
 *
 * Portada 1-a-1 desde la `<Routes>` de la versión React:
 *
 *   `/`          → redirige a `/cars`
 *   `/login`     → pública (login)
 *   `/register`  → pública (registro)
 *   `/cars`      → protegida por `authGuard` (equiv. a `<ProtectedRoute>`)
 *   `**`         → página 404
 *
 * `loadComponent` se usa en lugar de `component` para aprovechar
 * lazy loading: cada pantalla se descarga como un chunk aparte, así
 * no cargamos el formulario de login si el usuario entra directo a
 * la tabla de autos con un token válido.
 */
export const APP_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'cars' },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.page').then((m) => m.LoginPage),
    title: 'Ingresar · Ufinet Autos',
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register.page').then((m) => m.RegisterPage),
    title: 'Registro · Ufinet Autos',
  },
  {
    path: 'cars',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/cars/cars.page').then((m) => m.CarsPage),
    title: 'Mis autos · Ufinet Autos',
  },
  {
    path: '**',
    loadComponent: () => import('./pages/not-found/not-found.page').then((m) => m.NotFoundPage),
    title: 'No encontrado · Ufinet Autos',
  },
];
