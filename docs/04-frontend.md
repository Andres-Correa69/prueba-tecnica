# 04 — Frontend

## Stack

- **Vite + React 18 + TypeScript**: HMR en milisegundos, ESM nativo, CRA
  está oficialmente obsoleto.
- **MUI v6 + X-Data-Grid**: tabla lista para paginación/acciones/sort
  sin escribirlo a mano.
- **react-router v6**: rutas declarativas.
- **react-hook-form + zod**: forms no controlados (menos re-renders) +
  validación con el mismo esquema que también escribimos en el backend.
- **axios**: interceptores son trivial de enganchar comparado con
  `fetch` a pelo.

## Estructura

```
frontend/src
├── api/           ← dos instancias axios (auth / cars) + clients tipados
├── auth/          ← AuthContext, ProtectedRoute, tokenStorage
├── pages/         ← LoginPage, RegisterPage, CarsPage, NotFoundPage
├── components/    ← NavBar, CarTable, CarFormDialog, ConfirmDeleteDialog, SearchFilters
├── schemas/       ← zod (login/register/car)
├── theme/         ← MUI createTheme
└── App.tsx + main.tsx
```

## Cómo se mueve el token

1. `LoginPage` llama `useAuth().login(u, p)`.
2. `AuthContext` llama `authApi.login` (usa `axiosAuth`, sin bearer).
3. Si 200: guarda `{token, userId, username, expiresAt}` en localStorage
   y actualiza el estado.
4. A partir de aquí, `axiosCars` (otra instancia) tiene un interceptor
   que lee de localStorage y mete `Authorization: Bearer …` en cada
   request.
5. Si alguna respuesta vuelve 401: el interceptor limpia localStorage y
   hace `window.location.replace('/login')`.

## `ProtectedRoute`

```tsx
if (!isAuthenticated) return <Navigate to="/login" replace state={{ from: location }} />;
```

Combinado con `AuthContext.useEffect` que detecta la expiración del
token y cierra sesión proactivamente, el usuario nunca queda "parado en
una pantalla autenticada con un token muerto".

## Trade-off localStorage vs httpOnly cookie

| Criterio       | localStorage (lo que hacemos)     | httpOnly cookie (lo que haría producción) |
| -------------- | --------------------------------- | ----------------------------------------- |
| XSS            | 🔴 cualquier script lo lee        | 🟢 JS no puede leerla                     |
| CSRF           | 🟢 no aplica                      | 🔴 hay que añadir tokens anti-CSRF        |
| Integración    | 🟢 trivial (3 líneas en axios)    | 🔴 `Set-Cookie` + `withCredentials` + CORS con credentials |
| Multi-origen   | 🟢 el FE puede vivir donde quiera | 🔴 mismo dominio / subdominios controlados |

Para la prueba técnica y como **explicación breve**: "elegimos
localStorage por velocidad de integración; en producción migraría a
httpOnly cookie + CSRF token". El cambio requerido está en
`docs/07-casos-de-cambio.md`.

## Validación de formularios

- `react-hook-form` mantiene valores y errores en refs → poco
  re-render.
- `zod` describe el shape + reglas en **un solo lugar**, tipa el
  `CarFormValues` y construye errores por campo.
- Los esquemas reflejan los del backend; si luego cambias la regla en
  el backend, cámbiala aquí también (`docs/07`).

## Responsive en 1 línea

- `Container maxWidth="lg"` limita el ancho.
- MUI `Grid size={{ xs: 12, sm: 6 }}` colapsa a 1 columna en móvil.
- `DataGrid autoHeight` + `flex` hace que la tabla se adapte sin
  scroll interno raro.
- `NavBar` esconde el username en `xs` (`display: { xs: 'none', sm: 'block' }`).
