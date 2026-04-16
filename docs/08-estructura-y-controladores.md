# 08 — Estructura del proyecto y mapa de archivos

> Objetivo de este documento: que puedas abrir el proyecto y en **30 segundos**
> decir "esto está aquí, aquello allá". Durante el examen, si te preguntan
> "¿dónde validas la placa?" respondas sin dudar: `domain/vo/Placa.java`.

---

## Vista general del repositorio

```
prueba-tecnica/
├── SETUP.md                          ← comandos listos para la demo (IP 192.168.2.2)
├── README.md                         ← quickstart
├── CLAUDE.md                         ← notas para Claude Code (ignóralo en la demo)
├── settings.gradle.kts               ← incluye services:auth-service, services:cars-service
├── build.gradle.kts                  ← config raíz (Boot 3.3.5, Java 21)
├── gradle.properties                 ← UTF-8 + flags de Gradle
│
├── services/
│   ├── auth-service/                 ← microservicio de autenticación (puerto 8081)
│   └── cars-service/                 ← microservicio de autos (puerto 8082)
│
├── frontend/                         ← Vite + React + MUI (puerto 5173)
│
├── infra/
│   ├── sql/{01,02,03}.sql            ← schemas, tablas, seed
│   ├── docker-compose.yml            ← SQL Edge
│   └── multipass/setup.md            ← guía de la VM
│
├── docs/
│   ├── 00-overview.md
│   ├── 01-arquitectura-hexagonal.md
│   ├── 02-microservicios.md
│   ├── 03-seguridad-jwt.md
│   ├── 04-frontend.md
│   ├── 05-comandos.md
│   ├── 06-que-estudiar.md
│   ├── 07-casos-de-cambio.md
│   ├── 08-estructura-y-controladores.md   ← este archivo
│   └── 09-glosario-conceptos.md
│
└── postman/
    ├── Ufinet-Autos.postman_collection.json
    ├── Ufinet-Autos.postman_environment.json
    └── README.md
```

---

## 🎯 ¿Dónde están los CONTROLADORES?

Los controladores REST son los "puntos de entrada" HTTP. Cada microservicio
tiene su propio controlador en el paquete `infrastructure.adapter.in.rest`:

### auth-service — 1 controlador

| Archivo | Endpoints que expone |
|---------|----------------------|
| [`services/auth-service/src/main/java/develope/auth/infrastructure/adapter/in/rest/AuthController.java`](../services/auth-service/src/main/java/develope/auth/infrastructure/adapter/in/rest/AuthController.java) | `POST /auth/register` · `POST /auth/login` |
| [`.../rest/GlobalExceptionHandler.java`](../services/auth-service/src/main/java/develope/auth/infrastructure/adapter/in/rest/GlobalExceptionHandler.java) | Traduce excepciones del dominio → códigos HTTP (400, 401, 409, 500) |

### cars-service — 1 controlador

| Archivo | Endpoints que expone |
|---------|----------------------|
| [`services/cars-service/src/main/java/develope/cars/infrastructure/adapter/in/rest/CarController.java`](../services/cars-service/src/main/java/develope/cars/infrastructure/adapter/in/rest/CarController.java) | `GET /cars` · `GET /cars/{id}` · `POST /cars` · `PUT /cars/{id}` · `DELETE /cars/{id}` |
| [`.../rest/GlobalExceptionHandler.java`](../services/cars-service/src/main/java/develope/cars/infrastructure/adapter/in/rest/GlobalExceptionHandler.java) | Traduce excepciones del dominio → HTTP (400, 404, 409, 500) |

> **Pregunta clásica de examen**: "¿dónde está el endpoint que crea un auto?"
> Respuesta: `CarController.java`, método `create(...)`. La lógica
> (validación, persistencia, etc.) NO está en el controlador — está en el
> caso de uso `CreateCarService.java`.

---

## 🧭 Guía "Busco X, ¿dónde está?" — auth-service

Base: `services/auth-service/src/main/java/develope/auth/`

### Dominio (lógica pura, sin Spring)

| Busco… | Archivo |
|--------|---------|
| La entidad User (inmutable, con factory `register` / `rehydrate`) | `domain/model/User.java` |
| El identificador del usuario (tipo fuerte, no UUID pelado) | `domain/model/UserId.java` |
| Las reglas del nombre de usuario (regex 3-30 chars, lowercase) | `domain/vo/Username.java` |
| El envoltorio de la contraseña hasheada | `domain/vo/PasswordHash.java` |
| La excepción "usuario ya existe" | `domain/exception/UsernameAlreadyExistsException.java` |
| La excepción "credenciales inválidas" | `domain/exception/InvalidCredentialsException.java` |

### Aplicación (casos de uso, también sin Spring)

| Busco… | Archivo |
|--------|---------|
| La lógica de registrar un usuario | `application/usecase/RegisterUserService.java` |
| La lógica de login + emisión de token | `application/usecase/LoginService.java` |
| El contrato de "registrar un usuario" | `application/port/in/RegisterUserUseCase.java` |
| El contrato de "hacer login" | `application/port/in/LoginUseCase.java` |
| El contrato de persistencia de usuarios | `application/port/out/UserRepositoryPort.java` |
| El contrato de hasheo de contraseñas | `application/port/out/PasswordHasherPort.java` |
| El contrato de emisión de tokens | `application/port/out/TokenIssuerPort.java` |
| Los commands (inputs inmutables para los use cases) | `application/port/in/RegisterUserCommand.java`, `LoginCommand.java` |
| El resultado del login (token + metadata) | `application/port/in/AuthResult.java` |

### Infraestructura (Spring, JPA, jjwt, adaptadores)

| Busco… | Archivo |
|--------|---------|
| El controlador REST | `infrastructure/adapter/in/rest/AuthController.java` |
| Los DTOs de entrada | `infrastructure/adapter/in/rest/dto/RegisterRequest.java`, `LoginRequest.java` |
| Los DTOs de salida | `infrastructure/adapter/in/rest/dto/RegisterResponse.java`, `LoginResponse.java`, `ErrorResponse.java` |
| El traductor de errores → HTTP | `infrastructure/adapter/in/rest/GlobalExceptionHandler.java` |
| La entidad JPA (mirror de la tabla) | `infrastructure/adapter/out/persistence/UserJpaEntity.java` |
| El repositorio Spring Data | `infrastructure/adapter/out/persistence/UserJpaRepository.java` |
| El mapeo entre dominio y JPA | `infrastructure/adapter/out/persistence/UserPersistenceMapper.java` |
| El adaptador que implementa UserRepositoryPort | `infrastructure/adapter/out/persistence/UserPersistenceAdapter.java` |
| El adaptador BCrypt | `infrastructure/adapter/out/security/BCryptPasswordHasherAdapter.java` |
| El emisor de JWTs (HS256, jjwt 0.12.x) | `infrastructure/adapter/out/security/JwtTokenIssuerAdapter.java` |
| Configuración de Spring Security | `infrastructure/config/SecurityConfig.java` |
| Configuración CORS | `infrastructure/config/CorsConfig.java` |
| Cableado manual de casos de uso | `infrastructure/config/BeanConfig.java` |
| Propiedades de JWT (secret, issuer, exp) | `infrastructure/config/JwtProperties.java` |
| Clase main | `AuthServiceApplication.java` |
| Configuración por perfil | `src/main/resources/application.yml`, `application-dev.yml` |

### Tests

| Busco… | Archivo |
|--------|---------|
| Test del use case de registro | `src/test/java/.../usecase/RegisterUserServiceTest.java` |
| Test del use case de login | `src/test/java/.../usecase/LoginServiceTest.java` |

---

## 🚗 Guía "Busco X, ¿dónde está?" — cars-service

Base: `services/cars-service/src/main/java/develope/cars/`

### Dominio

| Busco… | Archivo |
|--------|---------|
| La entidad Car (inmutable, con `ensureOwnedBy`) | `domain/model/Car.java` |
| Identificador del carro | `domain/model/CarId.java` |
| Identificador del dueño | `domain/model/OwnerId.java` |
| **Validación de placa (regex)** | `domain/vo/Placa.java` |
| Validación de marca | `domain/vo/Marca.java` |
| Validación de modelo | `domain/vo/Modelo.java` |
| Validación de año (1900..año+1) | `domain/vo/Anio.java` |
| Validación de color | `domain/vo/Color.java` |
| Validación de URL de foto | `domain/vo/FotoUrl.java` |
| Excepción "no encontrado" | `domain/exception/CarNotFoundException.java` |
| Excepción "placa duplicada" | `domain/exception/PlacaAlreadyExistsException.java` |
| Excepción "no es tu carro" (anti-IDOR) | `domain/exception/CarNotOwnedByUserException.java` |

### Aplicación (5 casos de uso)

| Busco… | Archivo |
|--------|---------|
| Crear un auto | `application/usecase/CreateCarService.java` |
| Editar un auto | `application/usecase/UpdateCarService.java` |
| Eliminar un auto | `application/usecase/DeleteCarService.java` |
| Obtener un auto por id | `application/usecase/GetCarService.java` |
| Listar autos del dueño (con filtros + paginación) | `application/usecase/ListCarsByOwnerService.java` |
| Contratos de los casos de uso | `application/port/in/*UseCase.java` |
| Inputs (commands + filtros + page request) | `application/port/in/CreateCarCommand.java`, `UpdateCarCommand.java`, `CarFilter.java` |
| Contrato del repositorio | `application/port/out/CarRepositoryPort.java` |
| Tipo paginado agnóstico al framework | `application/port/out/DomainPage.java` |

### Infraestructura

| Busco… | Archivo |
|--------|---------|
| El controlador REST | `infrastructure/adapter/in/rest/CarController.java` |
| DTOs de entrada | `infrastructure/adapter/in/rest/dto/CarRequest.java` |
| DTOs de salida | `infrastructure/adapter/in/rest/dto/CarResponse.java`, `CarListResponse.java`, `ErrorResponse.java` |
| Traductor de errores | `infrastructure/adapter/in/rest/GlobalExceptionHandler.java` |
| Entidad JPA | `infrastructure/adapter/out/persistence/CarJpaEntity.java` |
| Spring Data JPA repo | `infrastructure/adapter/out/persistence/CarJpaRepository.java` |
| **Predicados de filtros null-safe** | `infrastructure/adapter/out/persistence/CarSpecifications.java` |
| Mapper dominio ↔ JPA | `infrastructure/adapter/out/persistence/CarPersistenceMapper.java` |
| Adaptador del repo | `infrastructure/adapter/out/persistence/CarPersistenceAdapter.java` |
| **Filtro JWT (valida cada request)** | `infrastructure/config/security/JwtAuthenticationFilter.java` |
| Decodificador de tokens | `infrastructure/config/security/JwtDecoder.java` |
| Carrier del usuario autenticado | `infrastructure/config/security/AuthenticatedUser.java` |
| Anotación @CurrentUser | `infrastructure/config/security/CurrentUser.java` |
| Resolver que inyecta @CurrentUser | `infrastructure/config/security/CurrentUserArgumentResolver.java` |
| Configuración de Spring Security | `infrastructure/config/SecurityConfig.java` |
| Registro del resolver en MVC | `infrastructure/config/WebMvcConfig.java` |
| Cableado de casos de uso | `infrastructure/config/BeanConfig.java` |

### Tests

| Busco… | Archivo |
|--------|---------|
| Test de `Placa` | `src/test/java/.../domain/vo/PlacaTest.java` |
| Test de `CreateCarService` | `src/test/java/.../application/usecase/CreateCarServiceTest.java` |

---

## 🎨 Guía "Busco X, ¿dónde está?" — Frontend

Base: `frontend/src/`

| Busco… | Archivo |
|--------|---------|
| Punto de entrada de React | `main.tsx` |
| Ruteo | `App.tsx` |
| Tema de MUI (colores, tipografía, radios) | `theme/index.ts` |
| Contexto de autenticación | `auth/AuthContext.tsx` |
| Guardia de rutas | `auth/ProtectedRoute.tsx` |
| Guardado del token en localStorage | `auth/tokenStorage.ts` |
| Cliente axios de auth-service | `api/axiosAuth.ts` |
| Cliente axios de cars-service (con interceptors) | `api/axiosCars.ts` |
| Llamadas a /auth/register, /auth/login | `api/authApi.ts` |
| Llamadas a /cars/* | `api/carsApi.ts` |
| Esquemas zod de login/registro | `schemas/authSchemas.ts` |
| Esquemas zod de crear/editar auto | `schemas/carSchemas.ts` |
| Página de login | `pages/LoginPage.tsx` |
| Página de registro | `pages/RegisterPage.tsx` |
| Página principal (tabla + stats + filtros) | `pages/CarsPage.tsx` |
| Página 404 | `pages/NotFoundPage.tsx` |
| Layout común login/registro | `components/AuthLayout.tsx` |
| Barra de navegación superior | `components/NavBar.tsx` |
| Tabla de autos (MUI DataGrid) | `components/CarTable.tsx` |
| Modal crear/editar | `components/CarFormDialog.tsx` |
| Modal de confirmación de borrado | `components/ConfirmDeleteDialog.tsx` |
| Barra de filtros con debounce | `components/SearchFilters.tsx` |

---

## 🗃️ Archivos SQL

| Archivo | Qué hace |
|---------|----------|
| `infra/sql/01-create-schemas.sql` | Crea los schemas `auth` y `cars` dentro de `ufinet_autos`. |
| `infra/sql/02-create-tables.sql` | Crea `auth.Users` y `cars.Cars` con sus índices y constraints. |
| `infra/sql/03-seed-data.sql` | Inserta 1 usuario demo + 3 autos (opcional). |

---

## 💡 "Regla de los 3 clics"

Durante la demo, si te preguntan algo, debes encontrarlo en máximo 3 clics:

1. **`/controladores`** → `infrastructure/adapter/in/rest/`
2. **`/lógica de negocio`** → `application/usecase/`
3. **`/reglas de validación de datos`** → `domain/vo/`
4. **`/configuración (Spring, security, JWT)`** → `infrastructure/config/`
5. **`/pantallas`** → `frontend/src/pages/`
6. **`/componentes reutilizables`** → `frontend/src/components/`

Memoriza esos 6 caminos y el mapa del repo queda en tu cabeza.
