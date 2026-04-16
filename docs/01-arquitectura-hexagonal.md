# 01 — Arquitectura Hexagonal (Ports & Adapters)

## Definición en 2 frases

La **hexagonal** (también llamada *ports and adapters*) separa la lógica
de negocio ("núcleo") de los detalles técnicos (web, BD, mensajería). El
núcleo **nunca** depende del framework; los frameworks dependen del núcleo
mediante interfaces ("puertos").

## Regla de dependencias

```
┌──────────────────────────── infrastructure ────────────────────────────┐
│  REST adapter (Spring MVC)     |     JPA adapter (Spring Data)         │
│  JWT filter / Security config  |     BCrypt adapter / jjwt adapter     │
└────────────────▲───────────────┴─────────────────▲────────────────────┘
                 │ implements                       │ implements
┌────────────────┴──────────────── application ────┴────────────────────┐
│   ports/in   : RegisterUserUseCase, CreateCarUseCase …               │
│   ports/out  : UserRepositoryPort, PasswordHasherPort, TokenIssuerPort│
│   usecase    : RegisterUserService, LoginService, CreateCarService … │
└────────────────▲──────────────────────────────────────────────────────┘
                 │ uses
┌────────────────┴──────────────── domain ──────────────────────────────┐
│   model      : User, Car  (aggregates, inmutables)                   │
│   vo         : Username, Placa, Anio, PasswordHash …                  │
│   exception  : UsernameAlreadyExistsException, CarNotFoundException  │
└───────────────────────────────────────────────────────────────────────┘

  Las flechas apuntan hacia ADENTRO. El núcleo ignora Spring y JPA.
```

## Cómo se traduce a paquetes en este proyecto

Para cada servicio (`auth` / `cars`):

```
develope.<svc>
├── domain
│   ├── model       ← agregados / entidades (inmutables, puros)
│   ├── vo          ← value objects con invariantes
│   └── exception   ← excepciones de negocio
├── application
│   ├── port
│   │   ├── in      ← interfaces + commands de use cases
│   │   └── out     ← interfaces de repo, hasher, token, …
│   └── usecase     ← implementaciones (Spring-free)
└── infrastructure
    ├── adapter
    │   ├── in/rest           ← controllers, DTOs, @RestControllerAdvice
    │   └── out/persistence   ← JPA entity, repo, adapter, mapper
    │   └── out/security      ← BCrypt, jjwt
    └── config                ← SecurityConfig, BeanConfig, JwtProperties
```

Regla mecánica: **buscas `import org.springframework`** en `domain/` o
`application/` y no debe aparecer. Si aparece, hay una fuga.

## Trazado end-to-end: `POST /auth/register`

1. **HTTP llega al `AuthController`** (`infrastructure/adapter/in/rest`).
   Jackson deserializa el body en `RegisterRequest` (DTO), Bean Validation
   dispara (`@NotBlank`, `@Size`).
2. El controller construye `RegisterUserCommand` y llama a
   `RegisterUserUseCase.register(...)` — es una **interface**, no conoce la
   implementación.
3. La interface la resuelve Spring al bean declarado en `BeanConfig`:
   `new RegisterUserService(userRepo, hasher)`.
4. `RegisterUserService` (application) valida el `Username` construyendo
   el VO (`Username.of(...)`), valida password, consulta
   `users.existsByUsername(...)` — otra interface, la implementación es
   `UserPersistenceAdapter` (infra).
5. Si no existe, llama a `hasher.hash(...)` —
   `BCryptPasswordHasherAdapter` (infra). Devuelve `PasswordHash`.
6. Construye `User.register(...)` (domain), lo persiste via el port.
7. Retorna el UUID → controller lo envuelve en `RegisterResponse` → 201.

**Observación clave**: los pasos 1, 5, 6 y 7 conocen Spring / JPA / Jackson.
Los pasos 2 y 4 NO. Si alguien cambia JPA por MongoDB, solo reescribe el
adapter; los pasos 2 y 4 no cambian.

## Beneficios concretos

- **Test rápido y determinístico**: `RegisterUserServiceTest` corre sin
  levantar Spring — usa un `Map<String, User>` como fake repo.
- **Reemplazar tecnología**: Argon2 en vez de BCrypt = un solo archivo.
- **Razonamiento local**: al leer `User.java` no ves anotaciones JPA que
  condicionen el diseño.
- **Evita el "fat service" que mezcla capas**: el `CarController` no abre
  transacciones ni sabe qué es una Specification.

## Costos (para ser honestos)

- Más archivos (interfaces + implementaciones). Para el tamaño de esta
  prueba se justifica; para un CRUD trivial sería sobreingeniería.
- El wiring manual en `BeanConfig` reemplaza `@Service` — es código más
  explícito pero también más que mantener.

## Mini-FAQ de defensa

- **"¿Por qué no `@Service` en los use cases?"** Porque anotarlos obligaría
  a tener `spring-context` en el classpath del paquete application —
  justo lo que queremos evitar. El wiring centralizado en `BeanConfig`
  hace explícito el grafo.
- **"¿Por qué el port devuelve `DomainPage` y no `Page` de Spring Data?"**
  Porque el application layer no debe depender de Spring. El adapter
  traduce `Page` ↔ `DomainPage`.
- **"¿Dónde van las transacciones?"** En el adapter de persistencia
  (`CarPersistenceAdapter` con `@Transactional`). Si más adelante un use
  case necesita atomicidad multi-adapter, se crea un decorador
  `TransactionalUseCase` en application.
