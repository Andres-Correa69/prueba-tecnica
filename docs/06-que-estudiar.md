# 06 — Qué estudiar para sustentar

> Glosario de "si te preguntan X, respondes Y" — con ejemplos concretos
> de este proyecto.

## Spring / Java

### IoC (Inversion of Control) y DI (Dependency Injection)
- **IoC**: el framework instancia los objetos y los conecta, no tú.
- **DI**: inyectar colaboradores por constructor (o setter / field).
- En este proyecto: `CarController` recibe sus 5 use cases por
  constructor; `BeanConfig` decide **qué implementación concreta**
  (`new CreateCarService(carsRepo)`).

### JPA vs Hibernate
- **JPA** es la *especificación* (interfaces estándar en Jakarta).
- **Hibernate** es la *implementación* que Spring Boot incluye por
  defecto.
- En este proyecto: entidades con `@Entity @Table`, repositorios
  `JpaRepository`, dialecto `SQLServerDialect`.

### `ddl-auto=validate`
- Spring no crea ni modifica tablas; solo verifica que los mapeos
  coincidan con la BD al arrancar. Si no coinciden, falla.
- Buena práctica en ambientes reales: la BD la gobiernan scripts
  (Flyway/Liquibase o, aquí, `.sql` manuales).

### `@Transactional`
- Abre una transacción, commit si todo OK, rollback en runtime
  exception.
- Dónde va: en nuestra arquitectura, en el adapter
  (`CarPersistenceAdapter`) para mantener el application layer
  Spring-free. Si algún día un use case necesita atomicidad
  multi-adapter → decorador `TransactionalUseCase` en application.

### Spring Security filter chain
- Cadena de filtros por request. Nosotros:
  - auth-service: solo `permitAll` + stateless + CSRF off.
  - cars-service: añade `JwtAuthenticationFilter` **antes** de
    `UsernamePasswordAuthenticationFilter`.
- Tras el filtro, `SecurityContextHolder.getContext().getAuthentication()`
  lleva el `AuthenticatedUser` como principal.

## Diseño

### Hexagonal vs N-capas
- N-capas: Controller → Service → Repository. Sencillo pero
  Service suele acabar atado a la tecnología de persistencia.
- Hexagonal: core (domain + application) en el centro, **no depende de
  nada**, tecnología entra por adaptadores.
- Indicador para saber cuándo aplicar hexagonal: "¿Puedo cambiar la BD
  sin reescribir el core?".

### SOLID en este proyecto
- **S** (Single Responsibility): `RegisterUserService` SOLO registra;
  `LoginService` SOLO autentica.
- **O** (Open/Closed): los use cases están cerrados al cambio pero
  abiertos a extensión vía decoradores (imagina un
  `RateLimitingLoginService implements LoginUseCase`).
- **L** (Liskov): `UserPersistenceAdapter` es sustituible por cualquier
  otra impl del puerto.
- **I** (Interface Segregation): puertos pequeños, no un god-port.
- **D** (Dependency Inversion): el core depende de interfaces
  (`PasswordHasherPort`), no de BCrypt directamente.

### Value Objects
- `Username`, `Placa`, `Anio`, `PasswordHash` — construyen con
  validación y son inmutables. Elimina la pregunta "¿dónde validar
  esto?": SIEMPRE en el VO.

## HTTP / REST

### Códigos usados
| Código | Cuándo                                           |
| ------ | ------------------------------------------------ |
| 200    | GET / PUT con respuesta                          |
| 201    | POST creación (con el recurso en el body)        |
| 204    | DELETE OK                                        |
| 400    | Bean-Validation / VO rechaza input               |
| 401    | Sin token / token inválido / credenciales malas  |
| 403    | Autenticado pero sin permiso (no lo usamos hoy)  |
| 404    | No existe, **o** no te pertenece (anti-IDOR)     |
| 409    | Conflicto de unicidad (username o placa repetida) |
| 500    | Error inesperado                                 |

### REST stateless
- Cada request carga **todo** lo necesario para ser procesada
  (Authorization header + body). El servidor NO mantiene sesión.
- Ventaja operacional: escalado horizontal trivial, rolling restarts
  sin dropear logins.

## Seguridad (detalle explicable)

- **JWT**: ver `03-seguridad-jwt.md`.
- **IDOR (Insecure Direct Object Reference)**: URL `/cars/abc-123` si
  solo verifico `findById` sin `AND user_id = ?` → puedo ver autos
  ajenos. Prevenido con `findByIdAndOwner`.
- **SQL injection**: mitigada porque NO concatenamos SQL; JPA usa
  parámetros bind.
- **CSRF**: API stateless con Bearer → no aplica. Si mañana usamos
  cookies → activar CSRF token.

## Conceptos bonus que pueden preguntar

- **Clean architecture** vs hexagonal: casi sinónimos; Clean añade más
  capas (use cases, entities, frameworks, interface adapters) pero la
  idea es la misma.
- **DDD tactical**: value objects + aggregates + repositories + domain
  events. Aquí usamos los 3 primeros.
- **Saga pattern**: para transacciones cross-servicio. No aplica aquí
  pero prepárate por si preguntan "¿y si tuvieras que crear un user Y
  un car en la misma operación?".
