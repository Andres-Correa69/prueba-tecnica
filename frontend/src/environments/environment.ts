/**
 * Configuración usada en build de producción (`ng build`).
 *
 * Angular resuelve este archivo por defecto y lo reemplaza por
 * `environment.development.ts` cuando se compila con la configuración
 * `development` (declarada en `angular.json` via `fileReplacements`).
 *
 * Mantener las dos URLs permite que el mismo bundle apunte a
 * microservicios distintos sin recompilar código — en producción las
 * inyectaríamos por variables de entorno del servidor estático.
 */
export const environment = {
  production: true,
  authUrl: 'http://localhost:8081',
  carsUrl: 'http://localhost:8082',
};
