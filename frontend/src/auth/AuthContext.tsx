import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import type { ReactNode } from 'react';
import { tokenStorage, isExpired, type StoredAuth } from './tokenStorage';
import { authApi } from '../api/authApi';

interface AuthState {
  user: { id: string; username: string } | null;
  token: string | null;
  isAuthenticated: boolean;
  login(username: string, password: string): Promise<void>;
  register(username: string, password: string): Promise<void>;
  logout(): void;
}

const AuthContext = createContext<AuthState | undefined>(undefined);

/**
 * Proveedor de autenticación de nivel superior.
 *
 * <p>Al montarse, rehidrata desde localStorage y descarta inmediatamente
 * el token si ha expirado — así un token caducado nunca provoca un 401
 * sorpresa después de que el usuario creía haber iniciado sesión.</p>
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [auth, setAuth] = useState<StoredAuth | null>(() => {
    const stored = tokenStorage.load();
    if (stored && !isExpired(stored.expiresAt)) return stored;
    if (stored) tokenStorage.clear();
    return null;
  });

  // Si el token expira mientras la pestaña está abierta, cerramos sesión de forma proactiva.
  useEffect(() => {
    if (!auth) return;
    const msUntilExpiry = Date.parse(auth.expiresAt) - Date.now();
    if (msUntilExpiry <= 0) {
      setAuth(null);
      tokenStorage.clear();
      return;
    }
    const t = window.setTimeout(() => {
      setAuth(null);
      tokenStorage.clear();
    }, msUntilExpiry);
    return () => window.clearTimeout(t);
  }, [auth]);

  const login = useCallback(async (username: string, password: string) => {
    const result = await authApi.login(username, password);
    tokenStorage.save(result);
    setAuth(result);
  }, []);

  const register = useCallback(async (username: string, password: string) => {
    await authApi.register(username, password);
    // Conveniencia de UX: auto-login justo después de un registro exitoso
    // para que el usuario aterrice directo en la página de autos.
    await login(username, password);
  }, [login]);

  const logout = useCallback(() => {
    tokenStorage.clear();
    setAuth(null);
  }, []);

  const value = useMemo<AuthState>(() => ({
    user: auth ? { id: auth.userId, username: auth.username } : null,
    token: auth?.token ?? null,
    isAuthenticated: Boolean(auth),
    login,
    register,
    logout,
  }), [auth, login, register, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthState {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>');
  return ctx;
}
