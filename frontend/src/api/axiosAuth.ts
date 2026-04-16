import axios from 'axios';

// Instancia de axios separada para el servicio de auth (no necesita bearer
// y tiene una base URL distinta). Usar dos instancias mantiene la pila de
// interceptores de cada una enfocada y fácil de razonar.
export const axiosAuth = axios.create({
  baseURL: import.meta.env.VITE_AUTH_URL,
  headers: { 'Content-Type': 'application/json' },
});
