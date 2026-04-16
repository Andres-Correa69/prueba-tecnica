# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Context

This is a technical-test project (`Prueba Ufinet Autos.docx` in repo root contains the full spec in Spanish). The goal is a full-stack application where authenticated users register and manage their cars.

Target scope per the spec:
- **Backend (this repo):** Spring Boot REST API, JWT auth via Spring Security, SQL Server persistence via JPA/Hibernate.
- **Frontend (not yet present):** React app with login + car CRUD screens, JWT stored client-side.
- **Database:** SQL Server with `users` and `cars` tables (`cars.user_id` FK to `users`); include a creation script.
- **Domain:** `Car` entity has `marca`, `modelo`, `año`, `placa`, `color`. Each user owns 0..N cars; endpoints must scope data to the authenticated user.

The repo is currently a **bare Spring Initializr scaffold** — no controllers, entities, security config, or DB wiring exist yet. When adding features, expect to create the full package structure (e.g. `controller`, `service`, `repository`, `entity`, `dto`, `security`) under `src/main/java/develope/pruebatecnica/`.

## Tech Stack

- **Java 21** (toolchain enforced in `build.gradle.kts`).
- **Spring Boot 4.0.5** with Gradle Kotlin DSL (`build.gradle.kts`, `settings.gradle.kts`). Note: 4.x is a very recent major version — when adding starters, check compatibility and prefer `spring-boot-starter-*` coordinates without explicit versions (let Boot's BOM manage them).
- **JUnit 5** via `useJUnitPlatform()`.
- Base package: `develope.pruebatecnica`.

## Common Commands

Use the Gradle wrapper (`./gradlew`) — do not rely on a system `gradle`.

```bash
./gradlew build              # compile + test + assemble
./gradlew bootRun            # run the Spring Boot app
./gradlew test               # run all tests
./gradlew test --tests develope.pruebatecnica.PruebaTecnicaApplicationTests.contextLoads  # single test
./gradlew clean
```

## Notes

- `HELP.md` is the Spring Initializr default and is gitignored — safe to ignore.
- `application.properties` only sets the app name; DB URL, JWT secret, etc. still need to be added (prefer environment-variable placeholders, not hardcoded secrets).
