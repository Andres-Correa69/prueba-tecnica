import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import { AuthApiService } from '../api/auth-api.service';
import { StoredAuth, TokenStorageService } from './token-storage.service';

/**
 * Servicio único de autenticación. Sustituye al `AuthContext` + `useAuth()`
 * de la versión React pero con la API idiomática de Angular:
 *
 *   - Estado expuesto como **signals** (`user`, `token`, `isAuthenticated`).
 *     Cualquier componente puede suscribirse llamando al signal en la
 *     plantilla; Angular se encarga del change-detection granular.
 *   - `login()` y `register()` devuelven `Promise` para que las páginas
 *     puedan usar `await` igual que en la versión React.
 *   - Al construirse (lo hace el inyector la primera vez que alguien lo
 *     necesita) rehidrata la sesión desde localStorage y programa un
 *     `setTimeout` para cerrar sesión automáticamente al llegar la
 *     fecha de expiración del JWT.
 *
 * <p>Importante: el servicio NO guarda la contraseña ni la vuelve a leer;
 * la mandamos directo al backend y almacenamos sólo el token que el
 * backend devuelve.</p>
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(AuthApiService);
  private readonly storage = inject(TokenStorageService);

  /**
   * Signal privado con la sesión en memoria. Se expone a componentes a
   * través de los computed `user`, `token`, `isAuthenticated`.
   */
  private readonly state = signal<StoredAuth | null>(null);

  readonly user = computed(() => {
    const s = this.state();
    return s ? { id: s.userId, username: s.username } : null;
  });
  readonly token = computed(() => this.state()?.token ?? null);
  readonly isAuthenticated = computed(() => this.state() !== null);

  /**
   * Handle del timer que va a limpiar la sesión al expirar el token.
   * Guardarlo nos deja cancelarlo si el usuario hace `logout()` antes
   * (evita que el timer golpee una sesión ya distinta después).
   */
  private expiryTimer: ReturnType<typeof setTimeout> | null = null;

  constructor() {
    const stored = this.storage.load();
    if (stored && !this.storage.isExpired(stored.expiresAt)) {
      this.state.set(stored);
      this.scheduleAutoLogout(stored.expiresAt);
    } else if (stored) {
      // Había un token pero ya venía expirado; limpiamos para que ningún
      // interceptor se ponga a mandar un Bearer caducado a cada request.
      this.storage.clear();
    }
  }

  async login(username: string, password: string): Promise<void> {
    const result = await firstValueFrom(this.api.login(username, password));
    this.storage.save(result);
    this.state.set(result);
    this.scheduleAutoLogout(result.expiresAt);
  }

  async register(username: string, password: string): Promise<void> {
    await firstValueFrom(this.api.register(username, password));
    // UX: auto-login tras registro exitoso. Misma decisión que la versión
    // React — el usuario no debería tener que repetir credenciales.
    await this.login(username, password);
  }

  logout(): void {
    if (this.expiryTimer) {
      clearTimeout(this.expiryTimer);
      this.expiryTimer = null;
    }
    this.storage.clear();
    this.state.set(null);
  }

  /**
   * Programa un `logout()` automático cuando llegue `expiresAt`. Evita
   * que el usuario siga clicando durante minutos con un token expirado
   * para luego comerse un 401 sorpresa al tocar el backend.
   */
  private scheduleAutoLogout(expiresAt: string): void {
    if (this.expiryTimer) clearTimeout(this.expiryTimer);
    const msUntilExpiry = Date.parse(expiresAt) - Date.now();
    if (msUntilExpiry <= 0) {
      this.logout();
      return;
    }
    this.expiryTimer = setTimeout(() => this.logout(), msUntilExpiry);
  }
}
