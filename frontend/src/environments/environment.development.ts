/**
 * Configuración usada en `ng serve` (dev server, puerto 4200).
 *
 * Reemplaza a `environment.ts` durante el build de desarrollo gracias a
 * `fileReplacements` en `angular.json`. Cambiar aquí las URLs si los
 * servicios corren en IPs distintas (p. ej. cuando los levantas dentro
 * de la VM Multipass en vez de localhost).
 */
export const environment = {
  production: false,
  authUrl: 'http://localhost:8081',
  carsUrl: 'http://localhost:8082',
};
