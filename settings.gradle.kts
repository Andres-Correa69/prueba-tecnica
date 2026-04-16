/*
 * Build multi-módulo para la prueba técnica de Ufinet Autos.
 *
 * Dividimos el backend en dos microservicios Spring Boot desplegables
 * de forma independiente, que viven como sub-proyectos de Gradle bajo
 * `services/`:
 *
 *   - auth-service : registro + login de usuarios (emite JWTs).
 *   - cars-service : CRUD de autos pertenecientes al usuario autenticado.
 *
 * Ambos servicios comparten el mismo secreto de firma JWT (HS256)
 * suministrado en tiempo de ejecución mediante la variable de entorno
 * `JWT_SECRET`, por eso no se requiere un gateway para este demo: cada
 * servicio valida los tokens de forma independiente.
 */
rootProject.name = "prueba-tecnica"

include("services:auth-service")
include("services:cars-service")
