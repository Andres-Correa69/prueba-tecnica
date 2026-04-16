# 04 — Frontend

## Stack

- **Angular 17 + TypeScript** (standalone components): cada pantalla y
  cada componente se auto-declara sus imports, eliminando el
  `@NgModule` intermedio. Lazy loading por ruta con `loadComponent`.
- **Angular Material 17 + CDK**: misma identidad de Material Design que
  antes teníamos en MUI; tabla, diálogos, paginator, toolbar, menú, form-field
  y botones salen de la caja.
- **Angular Router**: rutas declarativas con `CanActivateFn` (`authGuard`)
  para la zona privada.
- **Reactive Forms + validadores manuales**: los formularios usan
  `FormGroup` y los `Validators` estándar + nuestros propios validadores
  (`form.validators.ts`) para la política de contraseña y el formato de
  placa. Mismos mensajes de error en español que en la versión anterior.
- **HttpClient + `HttpInterceptorFn` funcional**: un único interceptor
  global se encarga de (1) añadir el `Authorization: Bearer …` sólo en
  las llamadas al `cars-service` y (2) limpiar el token y redirigir al
  login cuando el backend responde 401.

## Estructura

```
frontend/
├── angular.json                     ← workspace (puerto 4200, file replacements)
├── package.json                     ← deps @angular/* 17.x, @angular/material 17.x
├── tsconfig*.json                   ← strict TS + strictTemplates
└── src/
    ├── index.html                   ← fuente Inter + <app-root>
    ├── main.ts                      ← bootstrapApplication(AppComponent, appConfig)
    ├── styles.scss                  ← theme global de Material (paleta portada de MUI)
    ├── environments/
    │   ├── environment.ts             ← prod (URLs por defecto)
    │   └── environment.development.ts ← dev (reemplaza a environment.ts vía angular.json)
    └── app/
        ├── app.component.ts         ← <router-outlet /> pelado
        ├── app.config.ts            ← providers: router, http, interceptor, animations, MDC defaults
        ├── app.routes.ts            ← rutas + authGuard
        ├── core/
        │   ├── api/
        │   │   ├── auth-api.service.ts   ← POST /auth/register, /auth/login
        │   │   └── cars-api.service.ts   ← CRUD /cars + query params
        │   └── auth/
        │       ├── token-storage.service.ts   ← localStorage (key 'jwt')
        │       ├── auth.service.ts            ← signals user/token/isAuthenticated, auto-logout
        │       ├── auth.guard.ts              ← CanActivateFn + returnUrl
        │       └── auth.interceptor.ts        ← Bearer + 401 handling
        ├── shared/
        │   ├── validators/form.validators.ts  ← equivalentes a los esquemas Zod
        │   └── components/
        │       ├── auth-layout/               ← fondo gradiente + card centrada
        │       ├── nav-bar/                   ← AppBar con menú de usuario
        │       ├── car-table/                 ← mat-table + mat-paginator (server-side)
        │       ├── car-form-dialog/           ← Material Dialog crear/editar
        │       ├── confirm-delete-dialog/     ← Material Dialog confirmar borrado
        │       └── search-filters/            ← 4 inputs con debounce 350 ms
        └── pages/
            ├── login/ · register/
            ├── cars/  · not-found/
```

## Cómo se mueve el token

1. `LoginPage` llama `AuthService.login(u, p)`.
2. `AuthService` llama `AuthApiService.login` (HttpClient → auth-service
   en puerto 8081). Al ser la URL del auth-service, el interceptor NO
   inyecta ningún Bearer.
3. Si 200 OK: guarda `{token, userId, username, expiresAt}` en
   localStorage bajo la key `'jwt'` y actualiza su `signal` interno.
   Agenda un `setTimeout` que hará `logout()` cuando llegue `expiresAt`.
4. A partir de aquí, toda llamada a `cars-service` (puerto 8082) pasa
   por el `authInterceptor`, que añade `Authorization: Bearer <token>`.
5. Si el backend responde 401: el mismo interceptor limpia el
   localStorage y navega a `/login` con `replaceUrl: true`.

## `authGuard` (antes `ProtectedRoute`)

```ts
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isAuthenticated()) return true;
  return router.createUrlTree(['/login'], {
    queryParams: { returnUrl: state.url },
  });
};
```

Combinado con el timer de expiración en `AuthService`, el usuario nunca
queda "parado en una pantalla autenticada con un token muerto".

## Trade-off localStorage vs httpOnly cookie

| Criterio     | localStorage (lo que hacemos)      | httpOnly cookie (lo que haría producción)                  |
| ------------ | ---------------------------------- | ---------------------------------------------------------- |
| XSS          | 🔴 cualquier script lo lee         | 🟢 JS no puede leerla                                      |
| CSRF         | 🟢 no aplica                       | 🔴 hay que añadir tokens anti-CSRF                         |
| Integración  | 🟢 trivial (una ruta en el interceptor) | 🔴 `Set-Cookie` + `withCredentials` + CORS con credentials |
| Multi-origen | 🟢 el FE puede vivir donde quiera  | 🔴 mismo dominio / subdominios controlados                 |

Explicación breve: "elegimos localStorage por velocidad de integración;
en producción migraría a httpOnly cookie + CSRF token". El cambio
concreto está documentado en `docs/07-casos-de-cambio.md`.

## Validación de formularios

- **Reactive Forms**: `FormGroup` + `FormControl` mantienen el estado
  reactivo y cada control emite `valueChanges`. `FormBuilder` reduce el
  boilerplate.
- **Validadores manuales reutilizables**
  (`src/app/shared/validators/form.validators.ts`):
  - `usernameValidators` → 3–30 chars, `[A-Za-z0-9._-]`.
  - `registerPasswordValidators` → 8–100 chars, letras + dígitos.
  - `placaValidators` → regex `^[A-Z0-9]{3}-?[A-Z0-9]{3}$`.
  - `anioValidators` → rango 1900 … `currentYear + 1`, entero.
  - `fotoUrlValidators` → opcional, si trae algo debe comenzar por `http://` o `https://`.
- **`firstErrorMessage()`** traduce los códigos de error (`required`,
  `minlength`, `pattern`, `passwordComposition`, …) a los mismos textos
  en español que mostrábamos antes.
- Los errores se muestran con `<mat-error>` y `<mat-hint>` del
  `mat-form-field`.

## Responsive en 1 línea

- `.app-container` limita el ancho a 1200 px y paddings acordes por
  breakpoint (igual que el antiguo `Container maxWidth="lg"`).
- `.cars-page__stats` y `.car-form__grid` usan `grid-template-columns:
  repeat(N, minmax(0, 1fr))` con media queries — colapsan a 1 columna
  en móvil.
- `mat-table` vive dentro de `.car-table__scroll { overflow-x: auto }`
  para que no rompa el layout en pantallas pequeñas.
- El `NavBarComponent` esconde el `username` en pantallas ≤ 600 px
  (`@media (max-width: 600px) { display: none }`), igual que el antiguo
  `display: { xs: 'none', sm: 'block' }` de MUI.

## Comandos de desarrollo

```bash
cd frontend
npm install                        # primera vez
npm run dev                        # ng serve --open → http://localhost:4200
npm run build                      # ng build (producción)
npm test                           # Karma + Jasmine
```

> El build de desarrollo aplica `fileReplacements` en `angular.json` que
> sustituye `environment.ts` por `environment.development.ts`. Si
> levantas los microservicios en otra IP (p. ej. dentro de la VM
> Multipass), edita esas URLs allí.
