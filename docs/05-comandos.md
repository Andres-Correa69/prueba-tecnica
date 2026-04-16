# 05 — Comandos (copy-paste en orden)

## 0. Pre-requisitos en el host Mac

- Java 21 (`java -version`).
- Multipass instalado (`brew install --cask multipass`).
- Docker Desktop **solo** si quieres ejecutar `docker run` desde el host
  para cargar los `.sql` (es lo que hacemos aquí).
- Node 18+ y npm.

## 1. VM + SQL Edge + BD

```bash
# Si aún no existe la VM:
#   multipass launch 22.04 --name close-bluebill --cpus 2 --memory 4G --disk 20G
# (si ya la tienes, solo asegúrate que esté corriendo con `multipass start close-bluebill`)
export VM_IP=$(multipass info close-bluebill | awk '/IPv4/ {print $2; exit}')
echo "VM_IP=$VM_IP"    # debería ser 192.168.2.2 en tu caso

# Instalar Docker dentro de la VM
multipass exec close-bluebill -- bash -c 'sudo apt-get update && sudo apt-get install -y docker.io && sudo usermod -aG docker ubuntu'
multipass restart close-bluebill

# Arrancar SQL Edge en la VM
multipass exec close-bluebill -- docker run -d --name sqledge --restart unless-stopped \
  -e "ACCEPT_EULA=1" -e "MSSQL_SA_PASSWORD=Ufinet#2026_Strong" \
  -p 1433:1433 -v sqlvol:/var/opt/mssql \
  mcr.microsoft.com/azure-sql-edge:latest
```

Si `azure-sql-edge` no te funciona en tu arquitectura, ver el fallback
con `mssql/server:2022-latest` en `infra/multipass/setup.md`.

## 2. Crear DB + schemas + tablas

```bash
cd /Users/andrescorrea69/Documents/GitHub/prueba-tecnica

# Crear la base
docker run --rm -v "$PWD/infra/sql:/sql" mcr.microsoft.com/mssql-tools:latest \
  /opt/mssql-tools/bin/sqlcmd -S $VM_IP,1433 -U sa -P 'Ufinet#2026_Strong' \
  -Q "IF DB_ID('ufinet_autos') IS NULL CREATE DATABASE ufinet_autos;"

# Schemas, tablas, seed
for f in 01-create-schemas.sql 02-create-tables.sql 03-seed-data.sql; do
  docker run --rm -v "$PWD/infra/sql:/sql" mcr.microsoft.com/mssql-tools:latest \
    /opt/mssql-tools/bin/sqlcmd -S $VM_IP,1433 -U sa -P 'Ufinet#2026_Strong' \
    -d ufinet_autos -i /sql/$f
done
```

## 3. Variables de entorno para los servicios

```bash
export SQL_HOST=$VM_IP
export SQL_PASSWORD='Ufinet#2026_Strong'
export JWT_SECRET='dev-secret-at-least-32-bytes-long!'
export SPRING_PROFILES_ACTIVE=dev
```

## 4. Arrancar los microservicios (en 2 terminales)

```bash
# Terminal 1 — auth-service :8081
./gradlew :services:auth-service:bootRun

# Terminal 2 — cars-service :8082
./gradlew :services:cars-service:bootRun
```

## 5. Frontend (3ra terminal)

```bash
cd frontend
npm install
npm run dev          # abre http://localhost:5173
```

## 6. Smoke test con curl

```bash
# Registrar
curl -s -X POST http://localhost:8081/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"andres","password":"Test1234!"}'

# Login → token
TOKEN=$(curl -s -X POST http://localhost:8081/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"andres","password":"Test1234!"}' | python3 -c 'import json,sys;print(json.load(sys.stdin)["token"])')
echo "TOKEN=$TOKEN"

# Crear auto
curl -s -X POST http://localhost:8082/cars \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"marca":"Toyota","modelo":"Corolla","anio":2020,"placa":"ABC123","color":"Rojo"}'

# Listar
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8082/cars

# Sin token → 401
curl -i http://localhost:8082/cars
```

## 7. Tests backend

```bash
./gradlew test
./gradlew :services:auth-service:test --tests 'develope.auth.application.usecase.RegisterUserServiceTest'
```

## Limpieza rápida

```bash
./gradlew clean
multipass exec close-bluebill -- docker rm -f sqledge
multipass stop close-bluebill
# multipass delete close-bluebill && multipass purge   # borra la VM entera
```
