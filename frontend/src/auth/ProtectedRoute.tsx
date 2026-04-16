import type { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext';

/**
 * Barrera de protección de rutas.
 *
 * <p>Envuelve cualquier página que requiera que el usuario haya iniciado
 * sesión. Si no está autenticado, redirigimos a /login y recordamos
 * desde dónde venía para que la página de login pueda devolverlo ahí
 * tras el inicio de sesión.</p>
 */
export function ProtectedRoute({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth();
  const location = useLocation();
  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }
  return <>{children}</>;
}
