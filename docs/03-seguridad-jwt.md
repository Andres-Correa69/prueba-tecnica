# 03 — Seguridad y JWT

## Flujo completo

```
1. POST /auth/register  {username, password}
     auth-service → BCrypt.hash → INSERT auth.Users → 201
2. POST /auth/login  {username, password}
     auth-service → BCrypt.matches → Jwts.builder().signWith(HMAC).compact()
     → 200 {token, userId, username, expiresAt}
3. GET /cars  Authorization: Bearer <token>
     cars-service → JwtAuthenticationFilter → decoder verifica firma + iss + exp
     → SecurityContextHolder.set(new UsernamePasswordAuthToken(AuthenticatedUser))
     → CarController lee @CurrentUser → llama GetCarUseCase con ownerId
4. Si el token es inválido/expirado → 401 JSON uniforme
```

## Estructura del JWT

HS256 (simétrico). Header fijo, payload:

```json
{
  "iss": "ufinet-autos",
  "sub": "5f6a...-uuid-del-usuario",
  "username": "andres",
  "iat": 1713200000,
  "exp": 1713203600
}
```

- `sub` = user id → usado por cars-service para filtrar "mis autos".
- `iss` = issuer → el decoder rechaza cualquier token con otro issuer.
- `exp` = expiración absoluta (Unix seconds).

## HS256 vs RS256 — ¿por qué elegimos HS256 aquí?

| Criterio                       | HS256 (simétrico)         | RS256 (asimétrico)          |
| ------------------------------ | ------------------------- | --------------------------- |
| Tamaño de la firma             | Menor                     | Mayor                       |
| Velocidad                      | Muy rápido                | Lento al firmar             |
| Rotación de claves             | Coordinada en ambos lados | Rotas la privada sin tocar los verificadores |
| Riesgo si se filtra el secreto | Cualquiera puede **emitir** tokens | Cualquiera puede **verificar** (lo que ya es público) |
| Complejidad operativa          | Baja                      | Necesitas JWKS endpoint     |

Para una prueba técnica con 2 servicios + 1 equipo, HS256 con un
`JWT_SECRET` en ambos es suficiente y lo más simple de explicar. En
arquitecturas multi-equipo, la recomendación es RS256 + JWKS.

## BCrypt — ¿por qué no SHA-256 o MD5?

- SHA-256/MD5 son hashes **rápidos**, diseñados para integridad, NO para
  contraseñas. Un atacante con una GPU puede intentar miles de millones
  por segundo.
- BCrypt es **lento a propósito** (cost factor configurable), incluye
  salt aleatorio por contraseña, y se ha probado resistente.
- Alternativas modernas: Argon2 (preferido por OWASP 2024), scrypt. En
  este proyecto intercambiar BCrypt por Argon2 solo toca
  `BCryptPasswordHasherAdapter` — por eso ponerlo detrás de un port es
  barato y valioso.

## CORS en el flujo

- El navegador, antes de `POST /auth/login` con content-type JSON, envía
  un **preflight** `OPTIONS` al mismo path.
- `CorsConfig` en ambos servicios lista `http://localhost:5173` como
  origen permitido y `Authorization, Content-Type` como headers.
- En producción lo reemplazas por el dominio real.

## Defensas ya aplicadas

| Amenaza                           | Mitigación                                                                 |
| --------------------------------- | -------------------------------------------------------------------------- |
| **Account enumeration** (timing)  | `LoginService` hace un BCrypt dummy si el usuario no existe (tiempo constante). |
| **Account enumeration** (mensaje) | 401 siempre dice "invalid credentials", nunca "usuario no existe".         |
| **IDOR** (coger autos ajenos)     | `findByIdAndOwner(carId, ownerId)`; el ownerId sale del JWT, no del body.  |
| **IDOR** (404 vs 403)             | Devolvemos 404, no 403, para no filtrar existencia de la fila.             |
| **SQL injection**                 | Todo va por JPA / Criteria — zero concatenación de strings.                |
| **CSRF**                          | API stateless + no cookies → CSRF no aplica.                               |
| **XSS que robe el JWT**           | (mitigación residual) CSP + no `v-html` + React escapa por defecto. Ver trade-off localStorage en `04-frontend.md`. |
| **Secreto débil**                 | `JwtTokenIssuerAdapter` rechaza claves `<32 bytes` al arrancar.            |

## Defensas pendientes (para la defensa de la prueba)

- **Rate limiting** en `/auth/login` (e.g. bucket4j o filtro Nginx).
- **Rotación de secreto** (plan: variable `JWT_SECRET` con blue/green).
- **Logout real** (blacklist de JWT con TTL = `exp - now` en Redis).
- **Refresh tokens** para no tener que re-loguear cada hora.
