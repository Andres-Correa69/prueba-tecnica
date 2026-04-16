# Colección Postman — Ufinet Autos

## Archivos

- `Ufinet-Autos.postman_collection.json` — colección con todas las peticiones.
- `Ufinet-Autos.postman_environment.json` — entorno con `authUrl`, `carsUrl`, `username`, `password`.

## Cómo usar

1. Abre Postman → **Import** → arrastra los dos archivos JSON.
2. Arriba a la derecha selecciona el entorno **Ufinet Autos (local)**.
3. Asegúrate de tener corriendo `auth-service` (`:8081`) y `cars-service` (`:8082`).
4. Ejecuta **Auth → Register** (una sola vez) o directamente **Auth → Login** si ya tienes usuario.
5. El `test script` del login guarda automáticamente el `token` en la variable de entorno.
6. De ahí en adelante, todas las peticiones de **Cars** ya van autenticadas.

## Orden sugerido para una demo

1. `Auth → Register` (201) — crea un usuario nuevo.
2. `Auth → Login` (200) — obtiene el JWT.
3. `Cars → Crear auto` (201) — crea el primer auto.
4. `Cars → Listar autos (sin filtros)` (200) — ves el auto creado + guarda `carId`.
5. `Cars → Actualizar auto` (200) — cambia el color.
6. `Cars → Obtener auto por id` (200) — verifica el cambio.
7. `Cars → Listar sin token (debe dar 401)` (401) — demuestra que la protección JWT funciona.
8. `Cars → Eliminar auto` (204) — limpia.

## Variables de entorno

| Variable | Ejemplo | Descripción |
|----------|---------|-------------|
| `authUrl` | `http://localhost:8081` | Base URL de auth-service. |
| `carsUrl` | `http://localhost:8082` | Base URL de cars-service. |
| `username` | `andres` | Usuario que usan Register/Login. |
| `password` | `Test1234!` | Contraseña correspondiente. |
| `token` | *(auto)* | Lo rellena el test script del Login. |
| `userId` | *(auto)* | Lo rellena el test script del Login. |
| `carId` | *(auto)* | Lo rellena "Crear auto" y "Listar autos". |

## Notas

- **Bearer auth centralizado**: la carpeta `Cars` tiene `auth: bearer → {{token}}` a nivel de carpeta, así cada request hereda el header `Authorization` sin duplicar configuración.
- **El test `Listar sin token (debe dar 401)`** desactiva la auth a nivel de request (`auth: noauth`) para demostrar que el filtro JWT funciona.
- Si el token expira (1 hora por defecto) simplemente vuelve a ejecutar **Login**.
