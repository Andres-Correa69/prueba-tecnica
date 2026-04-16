import axios from 'axios';
import { tokenStorage } from '../auth/tokenStorage';

/**
 * Instancia de axios para el servicio de autos.
 *
 * Dos interceptores:
 *   - request: lee el JWT de localStorage e inyecta el header Bearer.
 *   - response: ante un 401, limpia el token y redirige el navegador a /login.
 */
export const axiosCars = axios.create({
  baseURL: import.meta.env.VITE_CARS_URL,
  headers: { 'Content-Type': 'application/json' },
});

axiosCars.interceptors.request.use((config) => {
  const stored = tokenStorage.load();
  if (stored?.token) {
    config.headers = config.headers ?? {};
    (config.headers as Record<string, string>)['Authorization'] = `Bearer ${stored.token}`;
  }
  return config;
});

axiosCars.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      tokenStorage.clear();
      // Una recarga completa es la forma más simple de resetear todo el estado de React y react-router.
      if (window.location.pathname !== '/login') {
        window.location.replace('/login');
      }
    }
    return Promise.reject(error);
  }
);
