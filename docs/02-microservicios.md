# 02 — Microservicios

## ¿Por qué 2 servicios y no uno solo?

- **Ritmo de cambio distinto**: el dominio de auth cambia rarísimo
  (login, registro, reset). El de autos cambia mucho (nuevos filtros,
  campos, fotos, validaciones). Separarlos permite desplegar cars-service
  10 veces al día sin tocar auth.
- **Escalado independiente**: si de repente hay muchas búsquedas de
  autos, escalas solo cars-service.
- **Frontera de responsabilidad clara**: cars-service JAMÁS toca la
  tabla de usuarios; todo lo que necesita saber del usuario viene en el
  JWT.
- **Superficie de ataque más pequeña en auth**: solo 2 endpoints
  públicos, el resto del sistema es authenticated-only.

## ¿Cómo se comunican los servicios?

**No se llaman entre sí.** Cars-service confía en el token firmado por
auth-service gracias al secreto HMAC compartido. Esto es:

- **Eventual consistency "trivial"**: no hay doble-commit a hacer.
- **Tolerancia a fallos**: auth-service caído ⇒ nadie nuevo puede
  loguearse, pero los ya logueados siguen operando hasta que su token
  expire.
- **Simplicidad operativa**: sin circuit breakers, sin timeouts
  cruzados.

Si mañana cars-service necesitara metadata de usuario (e.g. email),
hay dos opciones:

1. Añadir el claim al JWT (método preferido, sin runtime coupling).
2. Consultar a auth-service por HTTP (añade acoplamiento operativo).

## Los datos

Dos **schemas** (`auth`, `cars`) en un mismo SQL Server. Es un
compromiso deliberado:

- Pro: simplifica la operación para la prueba técnica.
- Contra: en producción "microservicios de verdad" tendrían instancias
  de BD separadas.

**No hay foreign key** `cars.user_id → auth.Users.id`. La integridad
referencial se delega al JWT: "si el token es válido y firmado por
auth-service, el `sub` es un user id legítimo".

## ¿Qué falta para un deployment real? (API Gateway)

Hoy, el frontend Angular conoce dos URLs (`environment.authUrl` y
`environment.carsUrl`, declaradas en `src/environments/environment*.ts`). En
producción lo habitual es poner un **Spring Cloud Gateway** o un
proxy/Nginx adelante:

```
Navegador ─▶ Gateway :8080 ─▶ auth :8081
                         └─▶ cars :8082
```

El gateway:
1. Valida el JWT **una sola vez** y propaga `X-User-Id` a los servicios.
2. Hace rate-limiting centralizado.
3. Simplifica CORS: solo un origen.
4. Permite publicar un OpenAPI único.

Migrar hoy implicaría añadir un módulo `services/gateway` con Spring
Cloud Gateway y quitar CORS + validación de token en cars-service.

## Otro siguiente paso razonable: RS256 + JWKS

En vez de compartir un secreto simétrico:

1. auth-service mantiene un **par de claves** (privada firma, pública se
   publica en `/.well-known/jwks.json`).
2. cars-service descarga y cachea el JWKS; valida firmas con la pública.
3. Ventaja: rotas la clave privada en auth sin tocar cars.

Cambio de código: reemplazar `JwtTokenIssuerAdapter` y `JwtDecoder` por
implementaciones RSA. El resto (`SecurityConfig`, filtro, puertos)
queda igual — otra vez la hexagonal pagando su costo.
