# 00 — Vista general

## Objetivo

Aplicación fullstack para que un usuario autenticado gestione sus autos
(`marca`, `modelo`, `año`, `placa`, `color`). El backend está partido en
**2 microservicios hexagonales**, la BD es **SQL Server** corriendo en un
contenedor dentro de una VM Multipass, y el frontend es **Angular 17 + Angular Material**.

## Diagrama

```
┌────────────────────┐        ┌────────────────────┐
│ Angular (:4200)    │──────▶│ auth-service 8081   │──┐
│ Angular Material   │        └────────────────────┘   │ JDBC
│ HttpClient + JWT   │        ┌────────────────────┐   │   ▼
│                    │──────▶│ cars-service 8082   │──┴──▶ SQL Edge :1433
└────────────────────┘ JWT    └────────────────────┘       (schemas auth, cars)
                                                             └─ VM Multipass + Docker
```

## Servicios y puertos

| Componente     | Puerto | Responsabilidad                              |
| -------------- | ------ | -------------------------------------------- |
| Frontend (dev) | 4200   | UI + almacenamiento de JWT                   |
| auth-service   | 8081   | `/auth/register`, `/auth/login`, emite JWT   |
| cars-service   | 8082   | CRUD de autos protegido con JWT              |
| SQL Edge       | 1433   | `ufinet_autos` DB, schemas `auth` y `cars`   |

## Secreto JWT

Ambos microservicios leen `JWT_SECRET` del entorno. auth-service firma con
HS256, cars-service valida con el mismo secreto. Sin API Gateway: cada
servicio verifica el token de forma independiente.

## Índice de la documentación

- [01 — Arquitectura hexagonal](01-arquitectura-hexagonal.md)
- [02 — Microservicios](02-microservicios.md)
- [03 — Seguridad y JWT](03-seguridad-jwt.md)
- [04 — Frontend](04-frontend.md)
- [05 — Comandos (paso a paso)](05-comandos.md)
- [06 — Qué estudiar para sustentar](06-que-estudiar.md)
- [07 — Casos de cambio](07-casos-de-cambio.md)
- [08 — Estructura y mapa de archivos (¿dónde está X?)](08-estructura-y-controladores.md)
- [09 — Glosario de conceptos](09-glosario-conceptos.md)
- [10 — Frontend: migración React → Angular](10-frontend-angular-migracion.md)
