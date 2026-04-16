/*
 * Datos semilla OPCIONALES.
 *
 * Inserta un usuario demo y tres autos demo para que el DataGrid no esté
 * vacío en una base de datos recién creada durante la demo. Ejecutar
 * este script es opcional — si lo omites, simplemente regístrate desde
 * la UI (`/register`) y crea los autos manualmente.
 *
 * Contraseña: "Demo1234!"
 * El hash de abajo se produjo con BCrypt cost=10 usando el mismo
 * BCryptPasswordEncoder que usa auth-service en tiempo de ejecución. Si
 * el encoder lo rechaza, regenéralo con:
 *
 *   curl -X POST http://localhost:8081/auth/register \
 *     -H 'Content-Type: application/json' \
 *     -d '{"username":"demo","password":"Demo1234!"}'
 *
 * Luego copia la fila resultante en este archivo.
 */
USE ufinet_autos;
GO

-- UUIDs estables para que los inserts sean idempotentes / repetibles.
DECLARE @demoUserId UNIQUEIDENTIFIER = '00000000-0000-0000-0000-000000000001';

IF NOT EXISTS (SELECT 1 FROM auth.Users WHERE username = N'demo')
BEGIN
    INSERT INTO auth.Users (id, username, password_hash)
    VALUES (
        @demoUserId,
        N'demo',
        -- Hash BCrypt($2a$10$...) de "Demo1234!" — regenéralo si el login falla.
        N'$2a$10$wFqL7VrKk8zP1f4Q0q5Wp.G6Y9gC2XQqFt0ZJ2yM2WZ3yE5pP3s8y'
    );
END;
GO

DECLARE @demoUserId UNIQUEIDENTIFIER = '00000000-0000-0000-0000-000000000001';

IF NOT EXISTS (SELECT 1 FROM cars.Cars WHERE placa = N'ABC123')
    INSERT INTO cars.Cars (id, user_id, marca, modelo, anio, placa, color)
    VALUES (NEWID(), @demoUserId, N'Toyota',    N'Corolla', 2020, N'ABC123', N'Rojo');

IF NOT EXISTS (SELECT 1 FROM cars.Cars WHERE placa = N'XYZ789')
    INSERT INTO cars.Cars (id, user_id, marca, modelo, anio, placa, color)
    VALUES (NEWID(), @demoUserId, N'Mazda',     N'CX-5',    2022, N'XYZ789', N'Azul');

IF NOT EXISTS (SELECT 1 FROM cars.Cars WHERE placa = N'QWE456')
    INSERT INTO cars.Cars (id, user_id, marca, modelo, anio, placa, color)
    VALUES (NEWID(), @demoUserId, N'Chevrolet', N'Spark',   2018, N'QWE456', N'Blanco');
GO
