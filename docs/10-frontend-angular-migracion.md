# 10 — Frontend: migración React → Angular

> Esta guía documenta el **cambio de stack del frontend** de Vite + React
> + MUI a **Angular 17 + Angular Material**. El backend (auth-service,
> cars-service, SQL Edge) no cambió: los contratos REST, los DTOs, el JWT
> HS256 y el esquema de BD siguen siendo idénticos. El único retoque
> fuera del frontend fue el **origen permitido por CORS** (de `5173` a
> `4200`) en los dos `application.yml`.

## Por qué Angular

- Es el requisito pedido por el nuevo contexto de la prueba ("en Angular,
  no React").
- Al estar tipado, con DI nativa, un router potente e HttpClient con
  interceptores idiomáticos, el mapeo conceptual desde un proyecto
  hexagonal de Spring es casi directo: cada "pieza" de React tenía un
  análogo natural en Angular. La sección *Mapa 1-a-1* lo lista.
- La versión previa (React + `useAuth()` + `axios` + `zod`) se podía
  replicar sin perder funcionalidad visible por el usuario.

## Decisiones clave de la migración

- **Angular 17 con _standalone components_**: evitamos el antiguo
  `AppModule`. Cada componente declara sus imports; los `providers`
  globales viven en `app.config.ts`. Menos archivos, menos ceremonia.
- **Angular Material 17 (en vez de MUI)**: misma familia de diseño
  (Material Design). La paleta de colores y la tipografía Inter se
  portaron a un tema SCSS en `src/styles.scss` para no perder identidad
  visual.
- **Reactive Forms + validadores propios (en vez de react-hook-form + zod)**:
  hablan el mismo idioma que los `FormControl` y los `<mat-form-field>`,
  sin dependencias extra. Los mensajes de error en español se preservan
  tal cual.
- **`HttpInterceptorFn` funcional (en vez de dos instancias axios
  distintas)**: un único interceptor registrado en `app.config.ts`
  decide por URL si añadir `Authorization: Bearer …`. Con eso dejamos
  de mantener dos clientes HTTP separados.
- **`signal()` para el estado local** en lugar de `useState`. El
  `AuthService` expone `user`, `token` e `isAuthenticated` como signals;
  los componentes los leen en la plantilla y Angular hace change
  detection granular.
- **Lazy loading por ruta** con `loadComponent`. Cada página se descarga
  como un chunk distinto; un usuario que entra directo a `/cars` con
  token válido jamás descarga el formulario de login.
- **`authGuard` funcional** (`CanActivateFn`) en vez de la clase
  legacy `CanActivate`. Preserva la URL original en `queryParams.returnUrl`
  para redirigir de vuelta tras autenticarse — mejora pequeña sobre el
  `ProtectedRoute` de React, que también lo guardaba pero en
  `location.state`.
- **Puerto 4200** (default del Angular CLI) en lugar de 5173. Obliga a
  actualizar `app.cors.allowed-origins` en los dos `application.yml`.

## Mapa 1-a-1: concepto React ↔ concepto Angular

| React (antes)                                           | Angular (ahora)                                                                 |
|---------------------------------------------------------|----------------------------------------------------------------------------------|
| `main.tsx` + `<BrowserRouter>` + `<AuthProvider>`       | `main.ts` + `bootstrapApplication(AppComponent, appConfig)`                     |
| `<Routes>` en `App.tsx`                                 | `APP_ROUTES` en `app.routes.ts`                                                 |
| `<ProtectedRoute>`                                      | `authGuard: CanActivateFn`                                                      |
| `AuthContext` + `useAuth()`                             | `AuthService` (`@Injectable`) con signals `user` / `isAuthenticated`            |
| `tokenStorage.ts`                                       | `TokenStorageService` (misma clave `'jwt'` en localStorage)                     |
| `axiosAuth` (sin Bearer) + `axiosCars` (con interceptor) | `HttpClient` + `authInterceptor` que filtra por URL                             |
| `authApi.ts`, `carsApi.ts`                              | `AuthApiService`, `CarsApiService` (observables)                                |
| `schemas/*.ts` (zod)                                    | `shared/validators/form.validators.ts` (Validators nativos + validadores propios) |
| `react-hook-form` + `zodResolver`                        | `ReactiveFormsModule` + `FormBuilder` + `firstErrorMessage(control)`             |
| MUI `createTheme`                                       | `@use '@angular/material' as mat;` + `define-light-theme` en `styles.scss`      |
| `<AuthLayout>`                                          | `<app-auth-layout>` con proyección de contenido (`<ng-content>`)                |
| `<NavBar>` con MUI `AppBar` + `Menu`                    | `<app-nav-bar>` con `mat-toolbar` + `mat-menu`                                  |
| `CarFormDialog` (MUI Dialog)                            | `CarFormDialogComponent` abierto con `MatDialog.open(...)`                      |
| `ConfirmDeleteDialog` (MUI Dialog)                      | `ConfirmDeleteDialogComponent` (mismo patrón)                                   |
| `CarTable` (MUI DataGrid, paginación server-side)       | `<app-car-table>` con `mat-table` + `mat-paginator` (server-side)               |
| `SearchFilters` con `useState` + `setTimeout` (debounce 350 ms) | `<app-search-filters>` con `form.valueChanges` + `debounceTime(350)` (RxJS) |
| `.env.development` + `import.meta.env.VITE_*`           | `src/environments/environment{.development}.ts` + `fileReplacements`            |

## Qué cambió fuera del frontend (y por qué)

- `services/auth-service/src/main/resources/application.yml` y
  `services/cars-service/src/main/resources/application.yml`:
  `app.cors.allowed-origins` pasó de `http://localhost:5173` a
  `http://localhost:4200`. Sin esto, el preflight `OPTIONS` de cualquier
  XHR desde `ng serve` sería bloqueado por el navegador.
- `services/auth-service/src/main/java/develope/auth/infrastructure/config/CorsConfig.java`:
  el JavaDoc decía "dev server de React"; se actualizó a "dev server de
  Angular". Es solo comentario — la lógica lee la lista desde YAML vía
  `@Value`.

Todo lo demás (dominio, casos de uso, adaptadores JPA, DTOs de entrada
/ salida, `JwtAuthenticationFilter` del cars-service, validaciones del
dominio como `Placa`, `Anio`, `Marca`…) siguió **intacto**. Esa es
precisamente la propiedad que vendíamos en el `docs/01-arquitectura-hexagonal.md`:
cambiar el adaptador de entrada más visible (el frontend) no toca el
núcleo.

## Paridad funcional garantizada

- **Registro**: mismas reglas (username 3–30, `[A-Za-z0-9._-]`; password
  ≥ 8, letras + dígitos), mismo flujo (tras 201, auto-login y
  navegación a `/cars`).
- **Login**: mismos códigos mostrados (401 → "Usuario o contraseña
  incorrectos"; resto → `error.message` o fallback).
- **CRUD de autos**: mismos campos, mismas validaciones (placa
  `^[A-Z0-9]{3}-?[A-Z0-9]{3}$`, año 1900–currentYear+1, fotoUrl
  opcional pero si trae algo debe ser `http(s)://`), mismas columnas,
  misma paginación server-side (5/10/20/50), mismos filtros con
  debounce 350 ms.
- **Expiración del token**: se detecta en cliente (timer al llegar
  `expiresAt`) y en servidor (filter del cars-service). En ambos casos
  el interceptor limpia el storage y redirige al login.
- **Trade-off localStorage**: idéntico. Mantenemos la clave `'jwt'`
  por si alguien tenía una sesión activa en el bundle anterior.

## Pendientes conocidos para endurecer la app

Estos items existían también en la versión React y siguen siendo
válidos tras la migración:

- Rate limiting en `/auth/login` (bucket4j o Nginx).
- Rotación del `JWT_SECRET` con blue/green.
- Logout real con blacklist de JWTs (Redis, TTL = `exp - now`).
- Refresh tokens para no re-loguear cada hora.
- Migración a httpOnly cookie + CSRF tokens (ver
  [`docs/07-casos-de-cambio.md`](07-casos-de-cambio.md) sección 4, ya
  actualizado con los pasos Angular).

## Lectura complementaria

- [`docs/04-frontend.md`](04-frontend.md) — referencia reescrita del
  frontend Angular (stack, estructura, flujo del token, responsive).
- [`docs/08-estructura-y-controladores.md`](08-estructura-y-controladores.md)
  — la tabla "Busco X, ¿dónde está?" del frontend ahora apunta a los
  archivos Angular.
- [`docs/09-glosario-conceptos.md`](09-glosario-conceptos.md) — entradas
  `mat-table + mat-paginator` y `Validación de formularios (Reactive Forms)`
  actualizadas; el resto de términos (JWT, BCrypt, CORS, Hexagonal, JPA…)
  sigue aplicando igual.
