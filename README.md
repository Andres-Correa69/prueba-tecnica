# Prueba Técnica — Ufinet Autos

App fullstack hexagonal: un usuario autenticado registra y administra sus
autos (`marca`, `modelo`, `año`, `placa`, `color`).

## Componentes

- **auth-service** (`:8081`) — Spring Boot 3.3, emite JWT HS256.
- **cars-service** (`:8082`) — Spring Boot 3.3, CRUD protegido con JWT.
- **frontend** (`:5173`) — Vite + React + MUI.
- **SQL Server** (`:1433`) — Azure SQL Edge en VM Multipass, schemas `auth` + `cars`.

## Quickstart (5 pasos)

```bash
# 1. VM + SQL Edge  (si ya tienes la VM `close-bluebill` creada, omite el launch)
# multipass launch 22.04 --name close-bluebill --cpus 2 --memory 4G --disk 20G
export VM_IP=$(multipass info close-bluebill | awk '/IPv4/ {print $2; exit}')  # ej. 192.168.2.2
multipass exec close-bluebill -- bash -c 'sudo apt-get update && sudo apt-get install -y docker.io && sudo usermod -aG docker ubuntu'
multipass restart close-bluebill
multipass exec close-bluebill -- docker run -d --name sqledge --restart unless-stopped \
  -e "ACCEPT_EULA=1" -e "MSSQL_SA_PASSWORD=Ufinet#2026_Strong" \
  -p 1433:1433 -v sqlvol:/var/opt/mssql mcr.microsoft.com/azure-sql-edge:latest

# 2. DB + schemas + tablas (+ seed opcional)
for f in 01-create-schemas.sql 02-create-tables.sql 03-seed-data.sql; do
  docker run --rm -v "$PWD/infra/sql:/sql" mcr.microsoft.com/mssql-tools:latest \
    /opt/mssql-tools/bin/sqlcmd -S $VM_IP,1433 -U sa -P 'Ufinet#2026_Strong' \
    -d ufinet_autos -i /sql/$f
done

# 3. Variables
export SQL_HOST=$VM_IP SQL_PASSWORD='Ufinet#2026_Strong' \
       JWT_SECRET='dev-secret-at-least-32-bytes-long!' SPRING_PROFILES_ACTIVE=dev

# 4. Backend (2 terminales)
./gradlew :services:auth-service:bootRun
./gradlew :services:cars-service:bootRun

# 5. Frontend
cd frontend && npm install && npm run dev
```

Detalles y troubleshooting: **[docs/05-comandos.md](docs/05-comandos.md)**.

## Documentación

| Archivo | Contenido |
|---------|-----------|
| [docs/00-overview.md](docs/00-overview.md) | Diagrama y vista general |
| [docs/01-arquitectura-hexagonal.md](docs/01-arquitectura-hexagonal.md) | Ports & adapters, trazado end-to-end |
| [docs/02-microservicios.md](docs/02-microservicios.md) | Por qué 2 servicios, cómo se comunican |
| [docs/03-seguridad-jwt.md](docs/03-seguridad-jwt.md) | BCrypt, JWT, CORS, amenazas |
| [docs/04-frontend.md](docs/04-frontend.md) | Vite, ProtectedRoute, axios, zod |
| [docs/05-comandos.md](docs/05-comandos.md) | Todos los comandos en orden |
| [docs/06-que-estudiar.md](docs/06-que-estudiar.md) | Glosario para sustentar la prueba |
| [docs/07-casos-de-cambio.md](docs/07-casos-de-cambio.md) | "Si me piden X, ¿dónde lo toco?" |

## Tests

```bash
./gradlew test
```
