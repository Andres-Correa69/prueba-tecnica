# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Context

Technical test: a fullstack app where an authenticated user registers and manages their cars
(`Prueba Ufinet Autos.docx` at repo root has the Spanish spec).

The backend is split into **two hexagonal Spring Boot microservices**:

- **auth-service** (`services/auth-service`, port `:8081`) — `/auth/register`, `/auth/login`, issues HS256 JWTs.
- **cars-service** (`services/cars-service`, port `:8082`) — CRUD of cars owned by the authenticated user.

Frontend: `frontend/` — Vite + React 18 + TypeScript + MUI + react-router v6 + react-hook-form + zod + axios.

Database: a single SQL Server (`ufinet_autos`) with two schemas (`auth`, `cars`) running inside
Azure SQL Edge in a Multipass VM. Creation scripts live in `infra/sql/`. There is **no cross-schema
foreign key** between `cars.Cars.user_id` and `auth.Users.id` — ownership is enforced in the
application layer using the JWT `sub` claim (see `docs/02-microservicios.md`).

Deep docs live in `docs/00-overview.md` (arch, hexagonal, microservices, JWT, FE, commands, study guide, change scenarios).

## Tech Stack

- **Java 21** (toolchain pinned in each service's Gradle build).
- **Spring Boot 3.3.5 LTS** (Gradle Kotlin DSL; 4.x was intentionally downgraded for ecosystem stability — see root `build.gradle.kts`).
- **jjwt 0.12.6** for HS256 (shared `JWT_SECRET` between services).
- **SQL Server** via `mssql-jdbc`, `ddl-auto=validate` so Hibernate never mutates the schema.
- **JUnit 5** via `useJUnitPlatform()`.

## Module layout

```
prueba-tecnica/
├── settings.gradle.kts        ← includes services:auth-service, services:cars-service
├── build.gradle.kts           ← root: subprojects{}; Boot/DM plugins `apply false`
├── services/
│   ├── auth-service/          ← hexagonal: domain/application/infrastructure
│   └── cars-service/          ← hexagonal + JWT auth filter + Specifications
├── frontend/                  ← Vite + React + MUI
├── infra/
│   ├── sql/*.sql              ← schemas + tables + optional seed
│   ├── docker-compose.yml
│   └── multipass/setup.md
└── docs/00-overview.md … 07-casos-de-cambio.md
```

Java base packages:
- `develope.auth.*` (auth-service)
- `develope.cars.*` (cars-service)

## Hexagonal rule (important)

`domain/` and `application/` must stay Spring/JPA/Jackson free. Spring annotations only appear in
`infrastructure/`. Use-case implementations are wired manually in each service's `BeanConfig` —
**do not** add `@Service` to them. If you need to add Spring to an application-layer class, stop
and reconsider (likely the logic belongs in an adapter instead).

Ownership is enforced both in the domain (`Car.ensureOwnedBy`) and in the repository port
(`findByIdAndOwner`). The JWT's `sub` claim is the single source of truth for the owner id — never
accept an owner id from a request body.

## Common Commands

Use the Gradle wrapper (`./gradlew`). Multi-module tasks:

```bash
./gradlew build                                             # all modules
./gradlew :services:auth-service:bootRun                    # run auth
./gradlew :services:cars-service:bootRun                    # run cars
./gradlew :services:auth-service:test
./gradlew :services:cars-service:test --tests 'develope.cars.domain.vo.PlacaTest'
./gradlew clean
```

Required env vars for `bootRun` with `dev` profile:

```bash
export SQL_HOST=<IP of Multipass VM>
export SQL_PASSWORD='Ufinet#2026_Strong'
export JWT_SECRET='dev-secret-at-least-32-bytes-long!'
export SPRING_PROFILES_ACTIVE=dev
```

Frontend:

```bash
cd frontend && npm install && npm run dev           # http://localhost:5173
```

## Notes

- `HELP.md` is the Spring Initializr leftover and is gitignored — safe to ignore.
- Full quickstart: `docs/05-comandos.md`.
- Change scenarios ("if they ask me to do X, where do I touch?"): `docs/07-casos-de-cambio.md`.
