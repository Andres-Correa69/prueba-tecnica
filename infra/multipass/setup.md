# Multipass + Docker + SQL Edge — paso a paso

> Objetivo: tener un SQL Server escuchando en `:1433` dentro de una VM Ubuntu
> (Multipass) que tu host Mac puede alcanzar por IP. Todos los servicios
> Spring se conectan a `jdbc:sqlserver://<IP_VM>:1433;databaseName=ufinet_autos;...`.

## 1. Crear / arrancar la VM

```bash
multipass launch 22.04 --name close-bluebill --cpus 2 --memory 4G --disk 20G
multipass info close-bluebill        # anota la IPv4 — la vamos a necesitar
```

Si ya existe:

```bash
multipass start close-bluebill
multipass shell close-bluebill
```

## 2. Instalar Docker dentro de la VM

```bash
sudo apt-get update
sudo apt-get install -y docker.io
sudo usermod -aG docker $USER
newgrp docker        # recarga el grupo sin volver a loguear
docker ps            # verifica
```

## 3. Levantar SQL Edge (dos opciones)

### Opción A — `docker run` directo (simple, sin compose)

```bash
docker volume create sqlvol
docker run -d --name sqledge --restart unless-stopped \
  -e "ACCEPT_EULA=1" \
  -e "MSSQL_SA_PASSWORD=Ufinet#2026_Strong" \
  -p 1433:1433 -v sqlvol:/var/opt/mssql \
  mcr.microsoft.com/azure-sql-edge:latest
```

### Opción B — usar `infra/docker-compose.yml`

1. Monta el repo dentro de la VM:
   ```bash
   multipass mount "$HOME/Documents/GitHub/prueba-tecnica" close-bluebill:/home/ubuntu/repo
   ```
2. Dentro de la VM:
   ```bash
   cd /home/ubuntu/repo/infra
   export SQL_PASSWORD='Ufinet#2026_Strong'
   docker compose up -d
   ```

## 4. Crear la base + ejecutar scripts

Desde el **host** (macOS), con el contenedor ya arriba:

```bash
VM_IP=$(multipass info close-bluebill | awk '/IPv4/ {print $2; exit}')
echo "VM_IP=$VM_IP"

# Crear la DB
docker run --rm -v "$PWD/infra/sql:/sql" \
  mcr.microsoft.com/mssql-tools:latest \
  /opt/mssql-tools/bin/sqlcmd -S $VM_IP,1433 -U sa -P 'Ufinet#2026_Strong' \
  -Q "IF DB_ID('ufinet_autos') IS NULL CREATE DATABASE ufinet_autos;"

# Ejecutar scripts en orden (schemas → tablas → seed opcional)
for f in 01-create-schemas.sql 02-create-tables.sql 03-seed-data.sql; do
  docker run --rm -v "$PWD/infra/sql:/sql" \
    mcr.microsoft.com/mssql-tools:latest \
    /opt/mssql-tools/bin/sqlcmd -S $VM_IP,1433 -U sa -P 'Ufinet#2026_Strong' \
    -d ufinet_autos -i /sql/$f
done
```

## 5. Probar conexión desde el host

```bash
docker run --rm -it mcr.microsoft.com/mssql-tools:latest \
  /opt/mssql-tools/bin/sqlcmd -S $VM_IP,1433 -U sa -P 'Ufinet#2026_Strong' \
  -d ufinet_autos -Q "SELECT name FROM sys.schemas WHERE name IN ('auth','cars');"
```

Debes ver `auth` y `cars`.

## 6. Exportar variables que los servicios Spring necesitan

```bash
export SQL_HOST="$VM_IP"
export SQL_PASSWORD='Ufinet#2026_Strong'
export JWT_SECRET='dev-secret-change-me-min-32-chars!'
```

## Troubleshooting

- **Puerto 1433 ocupado en el host**: no afecta — la VM expone 1433 en su
  propia IP. Conectas usando `$VM_IP:1433`, no `localhost:1433`.
- **Password rechazado por SQL Edge**: debe cumplir complejidad (mayúscula,
  minúscula, número, símbolo, ≥ 8 chars).
- **En x86-64 prefieres SQL Server estándar**: cambia la imagen en compose a
  `mcr.microsoft.com/mssql/server:2022-latest` y agrega `platform: linux/amd64`
  si estás en Apple Silicon.
