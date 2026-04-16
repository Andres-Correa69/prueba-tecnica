/*
 * Creación de tablas.
 *
 * Decisiones de diseño:
 *   - Llaves primarias UNIQUEIDENTIFIER: los UUID evitan filtrar la
 *     cantidad de filas, permiten que los clientes generen IDs de forma
 *     segura, y mantienen opacos los tokens (donde el claim `sub` es el
 *     id del usuario).
 *   - NVARCHAR para todo el texto: el proyecto se expondrá en español,
 *     los acentos y tildes requieren Unicode.
 *   - created_at / updated_at con default SYSUTCDATETIME(): UTC en todos
 *     lados — la capa de la JVM también usa Instant, así que nunca
 *     mezclamos zonas horarias.
 *   - DDL controlado por este script, no por Hibernate `ddl-auto=update`.
 *     Razón: en producción se quieren migraciones explícitas y
 *     revisables. Los servicios Spring corren con `ddl-auto=validate`,
 *     que falla de inmediato si una entidad Java se desvía del esquema
 *     de la BD (p.ej., alguien agrega un campo sin migración).
 */
USE ufinet_autos;
GO

IF OBJECT_ID('auth.Users', 'U') IS NULL
BEGIN
    CREATE TABLE auth.Users (
        id            UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
        username      NVARCHAR(30)     NOT NULL,
        password_hash NVARCHAR(100)    NOT NULL,
        created_at    DATETIME2(3)     NOT NULL CONSTRAINT DF_Users_CreatedAt DEFAULT SYSUTCDATETIME(),
        CONSTRAINT UQ_Users_Username UNIQUE (username)
    );
END;
GO

IF OBJECT_ID('cars.Cars', 'U') IS NULL
BEGIN
    CREATE TABLE cars.Cars (
        id         UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
        user_id    UNIQUEIDENTIFIER NOT NULL,                -- refleja auth.Users.id; sin FK por diseño (entre esquemas / microservicios)
        marca      NVARCHAR(50)     NOT NULL,
        modelo     NVARCHAR(50)     NOT NULL,
        anio       INT              NOT NULL,
        placa      NVARCHAR(10)     NOT NULL,
        color      NVARCHAR(30)     NOT NULL,
        foto_url   NVARCHAR(500)    NULL,
        created_at DATETIME2(3)     NOT NULL CONSTRAINT DF_Cars_CreatedAt DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2(3)     NOT NULL CONSTRAINT DF_Cars_UpdatedAt DEFAULT SYSUTCDATETIME(),
        CONSTRAINT UQ_Cars_Placa UNIQUE (placa),
        CONSTRAINT CK_Cars_Anio  CHECK (anio BETWEEN 1900 AND 2100)
    );
    CREATE INDEX IX_Cars_UserId ON cars.Cars(user_id);
    CREATE INDEX IX_Cars_Marca  ON cars.Cars(marca);
    CREATE INDEX IX_Cars_Anio   ON cars.Cars(anio);
END;
GO
