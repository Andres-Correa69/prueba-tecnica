# 07 — Casos de cambio ("si me piden X, ¿dónde lo toco?")

> Esta guía es el "as de la manga" del día del examen: ante una
> pregunta de modificación, sabes exactamente qué archivos tocar.

---

## 1. Agregar un campo nuevo (ejemplo: `vin`)

1. **SQL**: `infra/sql/02-create-tables.sql` → `ALTER TABLE cars.Cars
   ADD vin NVARCHAR(17) NULL;`
2. **Domain**: crear `domain/vo/Vin.java` con regex 17-chars alfanumérico.
3. **Aggregate**: añadir `Vin` en `Car` (constructor privado +
   `withUpdates` + factory).
4. **JPA entity**: `CarJpaEntity` + getters/setters + `@Column(name="vin")`.
5. **Mapper**: `CarPersistenceMapper.toEntity/toDomain/applyUpdates`.
6. **Commands**: `CreateCarCommand` / `UpdateCarCommand` añadir `String vin`.
7. **Use cases**: `CreateCarService`, `UpdateCarService` pasar a `Vin.of(...)`.
8. **DTOs REST**: `CarRequest`, `CarResponse`.
9. **Frontend (Angular)**:
   - `src/app/core/api/cars-api.service.ts` → interfaces `Car` y `CarInput`.
   - `src/app/shared/validators/form.validators.ts` → validador
     dedicado (regex) + entrada en `firstErrorMessage()`.
   - `src/app/shared/components/car-form-dialog/car-form-dialog.component.ts`
     → añadir un `FormControl` y un `mat-form-field` en la grilla.
   - Opcional: `src/app/shared/components/car-table/car-table.component.ts`
     → añadir columna al `columns[]` + `ng-container matColumnDef`.

Ruta crítica: si `ddl-auto=validate` no cuadra con el schema, la app no
arranca y te avisa temprano.

---

## 2. Cambiar SQL Server por PostgreSQL

1. `services/*/build.gradle.kts`: sustituir `mssql-jdbc` por
   `org.postgresql:postgresql`.
2. `application-dev.yml`: nueva URL
   `jdbc:postgresql://...`, dialecto `PostgreSQLDialect`.
3. `infra/sql/*.sql`: traducir (`UNIQUEIDENTIFIER` → `UUID`,
   `NVARCHAR` → `VARCHAR`, `SYSUTCDATETIME()` → `now() at time zone 'UTC'`).
4. El código Java **no cambia** (core + adapters JPA siguen válidos).

---

## 3. Agregar login con Google (OAuth2)

Dos caminos:

- **Rápido**: nuevo use case `LoginWithGoogleUseCase` que recibe el
  `id_token` de Google, lo valida con la librería oficial, busca/crea
  el User, emite nuestro JWT interno. Nuevo endpoint
  `/auth/login/google`. Frontend añade botón "Continuar con Google".
- **Estándar Spring**: `spring-boot-starter-oauth2-client` con
  `SecurityConfig.oauth2Login(...)`.

En ambos el resto del sistema (cars-service, frontend de autos) no se
entera — siguen viendo un JWT HS256 válido.

---

## 4. localStorage → httpOnly cookie

1. Auth-service: cambia el response de `/login` de `{token: ...}` a
   `Set-Cookie: jwt=...; HttpOnly; Secure; SameSite=Strict`.
2. Cars-service: `JwtAuthenticationFilter` lee `request.getCookies()`
   en vez de header `Authorization`.
3. `CorsConfig` (ambos): `setAllowCredentials(true)` y NO permitir
   `*` en origins.
4. Frontend (Angular):
   - En `HttpClient` pasar `{ withCredentials: true }` en cada request
     (o registrarlo globalmente con un interceptor trivial).
   - Borrar `TokenStorageService` y la rama que añade el Bearer en
     `auth.interceptor.ts`; dejar solo el manejo de 401.
5. Añadir CSRF: Angular tiene `HttpClientXsrfModule` que lee la cookie
   `XSRF-TOKEN` y manda `X-XSRF-TOKEN` automáticamente; el backend debe
   emitirla.

---

## 5. Paginación / filtros

Ya está. `GET /cars?page=1&size=20&sort=placa,asc&placa=ABC&modelo=corolla&marca=toyota&anio=2020`.
Los predicados null-safe están en `CarSpecifications`.

Para extender (ej. rango de años), añades dos query params (`anioFrom`,
`anioTo`) en el controller + método privado `anioBetween(...)` en
`CarSpecifications`.

---

## 6. Introducir API Gateway

1. `services/gateway/` con Spring Cloud Gateway.
2. Routes: `/auth/**` → `:8081`, `/cars/**` → `:8082`.
3. Gateway valida el JWT una vez y propaga `X-User-Id`.
4. En cars-service, simplificas: eliminas `JwtAuthenticationFilter`
   y lees el id del header.

Trade-off: un nodo más que operar; se gana punto único de rate-limit y
CORS.

---

## 7. Publicar eventos de dominio en Kafka

1. Nuevo puerto: `DomainEventPublisherPort`.
2. En `CreateCarService`, después de `cars.save(car)`, llama a
   `publisher.publish(new CarCreated(...))`.
3. Adapter `KafkaDomainEventPublisher` implementa el puerto.

Core sigue igual. Si mañana el mensajero es RabbitMQ, solo cambia el
adapter.

---

## 8. Cambiar BCrypt por Argon2

Solo `BCryptPasswordHasherAdapter` → reemplázalo por un
`Argon2PasswordHasherAdapter` (Spring Security trae
`Argon2PasswordEncoder`). Un hash BCrypt existente se migra al
siguiente login (detectas el prefijo `$2a$` y rehasheas).

---

## 9. Agregar rol ADMIN

1. auth-service: añadir claim `roles: ["ADMIN"]` en `JwtTokenIssuerAdapter`.
2. cars-service: en `JwtAuthenticationFilter`, convierte el claim en
   `List<SimpleGrantedAuthority>` y pásalo al `UsernamePasswordAuthenticationToken`.
3. Anotar endpoints admin con `@PreAuthorize("hasAuthority('ADMIN')")`.
4. Registrar `@EnableMethodSecurity` en `SecurityConfig`.
5. Nuevos use cases admin-only (p.ej. `ListAllCarsUseCase`).

---

## 10. Regla "año ≤ año actual" (sin +1)

Solo `domain/vo/Anio.java`: cambia `Year.now().getValue() + 1` por
`Year.now().getValue()`. Un solo archivo, un solo test. ESO es por lo
que la hexagonal vale la pena.
