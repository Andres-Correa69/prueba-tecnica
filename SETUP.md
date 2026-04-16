# SETUP.md — Comandos listos (IP de tu VM: `192.168.2.2`)

> Asumo que ya tienes la VM `close-bluebill` creada y corriendo en **192.168.2.2**.
> Todos los comandos de host los ejecutas desde la raíz del repo:
> `/Users/andrescorrea69/Documents/GitHub/prueba-tecnica`.

---

## 1. Crear el contenedor SQL Edge dentro de la VM

```bash
# Entrar a la VM
multipass shell close-bluebill
```

Una vez dentro de la VM:

```bash
# Instalar Docker (si aún no está)
sudo apt-get update && sudo apt-get install -y docker.io
sudo usermod -aG docker $USER
# Salir y volver a entrar para que el grupo tome efecto, o:
newgrp docker
docker ps      # debe responder sin 'permission denied'

# Crear volumen persistente + contenedor
docker volume create sqlvol
docker run -d --name sqledge --restart unless-stopped \
  -e "ACCEPT_EULA=1" \
  -e "MSSQL_SA_PASSWORD=Ufinet#2026_Strong" \
  -p 1433:1433 -v sqlvol:/var/opt/mssql \
  mcr.microsoft.com/azure-sql-edge:latest

# Verificar
docker ps              # debe aparecer sqledge
docker logs sqledge    # no debe tener errores, al final dice "SQL Server is now ready for client connections"
```

Si estás en Apple Silicon y `azure-sql-edge` falla, prueba:

```bash
docker run -d --name sqledge --restart unless-stopped --platform linux/amd64 \
  -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=Ufinet#2026_Strong" \
  -p 1433:1433 -v sqlvol:/var/opt/mssql \
  mcr.microsoft.com/mssql/server:2022-latest
```

**Sal de la VM:** `exit`

---

## 2. Probar conexión desde el host Mac

```bash
# Opción A: con Docker en el host (necesitas Docker Desktop)
docker run --rm -it mcr.microsoft.com/mssql-tools:latest \
  /opt/mssql-tools/bin/sqlcmd -S 192.168.2.2,1433 -U sa -P 'Ufinet#2026_Strong' \
  -Q "SELECT @@VERSION;"
```

```bash
# Opción B: sin Docker en el host, usando sqlcmd instalado (brew install sqlcmd)
sqlcmd -S 192.168.2.2,1433 -U sa -P 'Ufinet#2026_Strong' -Q "SELECT @@VERSION;"
```

```bash
# Opción C: sin sqlcmd, ejecutar los scripts dentro de la VM
multipass transfer infra/sql/01-create-schemas.sql close-bluebill:/tmp/01.sql
multipass transfer infra/sql/02-create-tables.sql  close-bluebill:/tmp/02.sql
multipass transfer infra/sql/03-seed-data.sql      close-bluebill:/tmp/03.sql
# Luego sigue la sección 3 opción "dentro de la VM"
```

Si te dice "Login failed" o timeout, verifica que el puerto 1433 está abierto en la VM:

```bash
nc -vz 192.168.2.2 1433     # "succeeded!"
```

---

## 3. Crear la BD, schemas, tablas (+ seed opcional)

### Opción recomendada: ejecutar desde el host con Docker

```bash
cd /Users/andrescorrea69/Documents/GitHub/prueba-tecnica

# 3.1 Crear la base de datos
docker run --rm -v "$PWD/infra/sql:/sql" mcr.microsoft.com/mssql-tools:latest \
  /opt/mssql-tools/bin/sqlcmd -S 192.168.2.2,1433 -U sa -P 'Ufinet#2026_Strong' \
  -Q "IF DB_ID('ufinet_autos') IS NULL CREATE DATABASE ufinet_autos;"

# 3.2 Ejecutar los scripts en orden
for f in 01-create-schemas.sql 02-create-tables.sql 03-seed-data.sql; do
  echo "==> Ejecutando $f"
  docker run --rm -v "$PWD/infra/sql:/sql" mcr.microsoft.com/mssql-tools:latest \
    /opt/mssql-tools/bin/sqlcmd -S 192.168.2.2,1433 -U sa -P 'Ufinet#2026_Strong' \
    -d ufinet_autos -i /sql/$f
done

# 3.3 Verificar
docker run --rm -it mcr.microsoft.com/mssql-tools:latest \
  /opt/mssql-tools/bin/sqlcmd -S 192.168.2.2,1433 -U sa -P 'Ufinet#2026_Strong' \
  -d ufinet_autos -Q "SELECT name FROM sys.schemas WHERE name IN ('auth','cars'); SELECT COUNT(*) AS users FROM auth.Users; SELECT COUNT(*) AS cars FROM cars.Cars;"
```

### Opción alterna: ejecutar dentro de la VM (si no tienes Docker en el host)

```bash
multipass shell close-bluebill

# Dentro de la VM:
docker run --rm -v /tmp:/sql mcr.microsoft.com/mssql-tools:latest \
  /opt/mssql-tools/bin/sqlcmd -S localhost,1433 -U sa -P 'Ufinet#2026_Strong' \
  -Q "IF DB_ID('ufinet_autos') IS NULL CREATE DATABASE ufinet_autos;"

for f in 01.sql 02.sql 03.sql; do
  docker run --rm -v /tmp:/sql mcr.microsoft.com/mssql-tools:latest \
    /opt/mssql-tools/bin/sqlcmd -S localhost,1433 -U sa -P 'Ufinet#2026_Strong' \
    -d ufinet_autos -i /sql/$f
done

exit
```

---

## 4. Exportar variables que consumen los servicios Spring

En la terminal donde vas a correr Gradle:

```bash
export SQL_HOST=192.168.2.2
export SQL_PASSWORD='Ufinet#2026_Strong'
export JWT_SECRET='dev-secret-at-least-32-bytes-long-ufinet-2026!'
export SPRING_PROFILES_ACTIVE=dev
```

**Importante**: los dos servicios (auth y cars) deben arrancar con **el mismo `JWT_SECRET`**. Si los arrancas en terminales distintas, exporta las 4 variables en cada una.

Cómo se conectan los servicios a la BD (ya configurado, solo para tu referencia — está en `services/*/src/main/resources/application-dev.yml`):

```
jdbc:sqlserver://192.168.2.2:1433;databaseName=ufinet_autos;encrypt=false;trustServerCertificate=true
  username: sa
  password: Ufinet#2026_Strong
```

---

## 5. Correr los servicios

Necesitas **3 terminales abiertas** (o `tmux`). En cada una, primero exporta las variables del paso 4.

### Terminal 1 — auth-service (puerto 8081)

```bash
cd /Users/andrescorrea69/Documents/GitHub/prueba-tecnica
export SQL_HOST=192.168.2.2 SQL_PASSWORD='Ufinet#2026_Strong' \
       JWT_SECRET='dev-secret-at-least-32-bytes-long-ufinet-2026!' \
       SPRING_PROFILES_ACTIVE=dev
./gradlew :services:auth-service:bootRun
```

Debes ver al final algo como: `Started AuthServiceApplication in 3.4s` + `Tomcat started on port 8081`.

### Terminal 2 — cars-service (puerto 8082)

```bash
cd /Users/andrescorrea69/Documents/GitHub/prueba-tecnica
export SQL_HOST=192.168.2.2 SQL_PASSWORD='Ufinet#2026_Strong' \
       JWT_SECRET='dev-secret-at-least-32-bytes-long-ufinet-2026!' \
       SPRING_PROFILES_ACTIVE=dev
./gradlew :services:cars-service:bootRun
```

### Terminal 3 — frontend Angular (puerto 4200)

```bash
cd /Users/andrescorrea69/Documents/GitHub/prueba-tecnica/frontend
npm install           # solo la primera vez (instala @angular/* 17, @angular/material 17)
npm run dev           # ejecuta `ng serve --open`
```

Abre el navegador en `http://localhost:4200`.

---

## 6. Probar el flujo completo

### A. Desde el navegador (lo más vistoso)

1. Abres `http://localhost:4200` → te redirige a `/login`.
2. Click en "Regístrate" → usuario `andres`, password `Test1234!` → te loguea automáticamente y te lleva a `/cars`.
3. Click en "Nuevo auto" → llena datos (marca `Toyota`, modelo `Corolla`, año `2020`, placa `ABC123`, color `Rojo`) → "Crear".
4. El auto aparece en la tabla. Prueba editar, buscar por placa, eliminar.
5. Click en "Salir" → vuelves a `/login`.

### B. Con curl (para demos)

```bash
# Registrar
curl -s -X POST http://localhost:8081/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"andres","password":"Test1234!"}'
# Respuesta: {"userId":"...","username":"andres"}  (HTTP 201)

# Login → guarda el token en una variable
TOKEN=$(curl -s -X POST http://localhost:8081/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"andres","password":"Test1234!"}' \
  | python3 -c 'import json,sys;print(json.load(sys.stdin)["token"])')
echo "TOKEN=$TOKEN"

# Crear un auto
curl -s -X POST http://localhost:8082/cars \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"marca":"Toyota","modelo":"Corolla","anio":2020,"placa":"ABC123","color":"Rojo"}'

# Listar mis autos
curl -s -H "Authorization: Bearer $TOKEN" "http://localhost:8082/cars?page=0&size=10"

# Sin token → 401
curl -i http://localhost:8082/cars

# Con filtros
curl -s -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8082/cars?placa=ABC&marca=Toyota&anio=2020"
```

---

## 7. Correr los tests (opcional, para demostrar en el examen)

```bash
cd /Users/andrescorrea69/Documents/GitHub/prueba-tecnica
./gradlew test
# Verás: 13 tests ejecutados, 13 passing
```

Para correr un test individual:

```bash
./gradlew :services:auth-service:test --tests 'develope.auth.application.usecase.RegisterUserServiceTest'
./gradlew :services:cars-service:test --tests 'develope.cars.domain.vo.PlacaTest'
```

---

## 8. Troubleshooting rápido

| Síntoma | Causa probable | Solución |
|---------|---------------|----------|
| `Login failed for user 'sa'` | Password no cumple complejidad | El que usamos (`Ufinet#2026_Strong`) ya cumple; reinicia contenedor. |
| Spring falla con `Cannot open database "ufinet_autos"` | No creaste la DB | Ejecuta paso 3.1. |
| Spring falla con `Schema-validation` | Hibernate vs tablas no cuadran | Re-ejecuta `02-create-tables.sql`. |
| Cars 401 en todas las rutas | JWT_SECRET distinto en cada servicio | Exporta la MISMA variable antes de `bootRun`. |
| Frontend CORS error | auth/cars NO tienen `http://localhost:4200` permitido | Ya está configurado; revisa que FE realmente corre en `:4200` y que el valor de `app.cors.allowed-origins` en ambos `application.yml` coincide. |
| `Connection timed out` al 192.168.2.2 | Firewall o VM no expone el puerto | Dentro de la VM: `sudo ufw status`; si está activo, `sudo ufw allow 1433/tcp`. |

---

## 9. Parar todo al final de la demo

```bash
# Ctrl+C en las 3 terminales para detener Spring + `ng serve`
# Detener el contenedor SQL (los datos se preservan en el volumen)
multipass exec close-bluebill -- docker stop sqledge
# O apagar la VM entera
multipass stop close-bluebill
```

Para volver a arrancar al día siguiente: `multipass start close-bluebill && multipass exec close-bluebill -- docker start sqledge`.
