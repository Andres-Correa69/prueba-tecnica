/**
 * Envoltura mínima sobre localStorage para el JWT.
 *
 * Compromiso: localStorage es legible por cualquier JS del mismo origen,
 * así que un XSS permitiría a un atacante robar el token. La alternativa
 * de nivel producción es una cookie httpOnly establecida por el backend
 * (inmune a lecturas de JS) combinada con tokens CSRF. Para un demo de
 * 1 día, localStorage es la integración más simple; el compromiso queda
 * documentado en `docs/04-frontend.md`.
 */
const KEY = 'jwt';

export interface StoredAuth {
  token: string;
  userId: string;
  username: string;
  expiresAt: string; // cadena ISO-8601
}

export const tokenStorage = {
  save(value: StoredAuth): void {
    localStorage.setItem(KEY, JSON.stringify(value));
  },
  load(): StoredAuth | null {
    const raw = localStorage.getItem(KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as StoredAuth;
    } catch {
      return null;
    }
  },
  clear(): void {
    localStorage.removeItem(KEY);
  },
};

export function isExpired(expiresAt: string): boolean {
  const exp = Date.parse(expiresAt);
  return !Number.isFinite(exp) || Date.now() >= exp;
}
