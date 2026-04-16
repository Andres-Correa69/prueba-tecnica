import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { StoredAuth } from '../auth/token-storage.service';

interface LoginResponse {
  token: string;
  userId: string;
  username: string;
  expiresAt: string;
}

/**
 * Cliente tipado de `auth-service` (puerto 8081). Paralelo de
 * `src/api/authApi.ts` en la versión React.
 *
 * - `register()` devuelve `void` porque el backend responde 201 sin
 *   cuerpo relevante para el cliente.
 * - `login()` devuelve `Observable<StoredAuth>` para integrarse con
 *   RxJS; las páginas lo convierten a Promise via `firstValueFrom`
 *   cuando necesitan `await`.
 */
@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.authUrl;

  register(username: string, password: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/auth/register`, { username, password });
  }

  login(username: string, password: string): Observable<StoredAuth> {
    return this.http
      .post<LoginResponse>(`${this.baseUrl}/auth/login`, { username, password })
      .pipe(
        map((data) => ({
          token: data.token,
          userId: data.userId,
          username: data.username,
          expiresAt: data.expiresAt,
        })),
      );
  }
}
