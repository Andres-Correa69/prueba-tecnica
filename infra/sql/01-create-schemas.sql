/*
 * Creación de esquemas.
 *
 * Una sola base de datos física (`ufinet_autos`) con dos esquemas lógicos:
 *   auth : dueño de la tabla Users (gestionada por auth-service).
 *   cars : dueño de la tabla Cars  (gestionada por cars-service).
 *
 * Por qué dos esquemas en vez de dos bases de datos:
 *   - Mantenemos las tablas de cada servicio lógicamente aisladas (en un
 *     despliegue real, cada microservicio sólo tiene credenciales
 *     acotadas a su propio esquema).
 *   - Sin FK entre esquemas: cars.user_id es un UNIQUEIDENTIFIER plano que
 *     debe coincidir con el claim `sub` del JWT presentado. La
 *     verificación de propiedad se aplica en la capa de *aplicación*
 *     (findByIdAndOwner), no a nivel de base de datos. Éste es el patrón
 *     estándar de microservicios: cada servicio es dueño de sus datos y
 *     confía en los IDs provenientes de los tokens.
 *
 * Idempotente: seguro de ejecutar repetidamente.
 */
USE ufinet_autos;
GO

IF NOT EXISTS (SELECT 1 FROM sys.schemas WHERE name = N'auth')
    EXEC('CREATE SCHEMA auth AUTHORIZATION dbo;');
GO

IF NOT EXISTS (SELECT 1 FROM sys.schemas WHERE name = N'cars')
    EXEC('CREATE SCHEMA cars AUTHORIZATION dbo;');
GO
