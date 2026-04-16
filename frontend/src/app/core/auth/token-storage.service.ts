import { Injectable } from '@angular/core';

/**
 * Forma del JWT almacenado en `localStorage`.
 *
 * Es idéntica al `StoredAuth` de la versión React, para no tener que
 * migrar tokens ya emitidos — el backend sigue devolviendo exactamente
 * estos campos en `POST /auth/login`.
 */
export interface StoredAuth {
  token: string;
  userId: string;
  username: string;
  /** ISO-8601 en UTC, tal como lo devuelve el backend (`Instant`). */
  expiresAt: string;
}

/**
 * Servicio de persistencia del JWT en `localStorage`.
 *
 * <p>Se expone como `@Injectable({ providedIn: 'root' })` para que sea
 * una instancia singleton inyectable desde cualquier parte (AuthService,
 * interceptor, guard). La clave usada es la misma que tenía la versión
 * React (`'jwt'`) para que, si un usuario tiene una sesión abierta en el
 * bundle anterior, pueda seguirla usando en el bundle Angular.</p>
 *
 * <p>Trade-off explícito: `localStorage` es legible por cualquier script
 * del mismo origen, así que un XSS permitiría exfiltrar el token. La
 * alternativa "nivel producción" es una cookie httpOnly emitida por el
 * backend + CSRF tokens. Para este demo aceptamos el riesgo a cambio de
 * la simplicidad de integración (no requiere cambios en el backend).</p>
 */
@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  private static readonly KEY = 'jwt';

  save(value: StoredAuth): void {
    localStorage.setItem(TokenStorageService.KEY, JSON.stringify(value));
  }

  load(): StoredAuth | null {
    const raw = localStorage.getItem(TokenStorageService.KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as StoredAuth;
    } catch {
      return null;
    }
  }

  clear(): void {
    localStorage.removeItem(TokenStorageService.KEY);
  }

  /**
   * Devuelve `true` si el token ya expiró o si la fecha no es parseable.
   * Usamos `Date.parse` (en vez de construir `new Date`) para aceptar el
   * formato exacto que emite Java `Instant.toString()` (`2026-04-16T…Z`).
   */
  isExpired(expiresAt: string): boolean {
    const exp = Date.parse(expiresAt);
    return !Number.isFinite(exp) || Date.now() >= exp;
  }
}
