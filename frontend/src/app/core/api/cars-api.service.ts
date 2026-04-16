import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

/** Auto tal cual lo devuelve el backend (`CarResponse` JSON). */
export interface Car {
  id: string;
  ownerId: string;
  marca: string;
  modelo: string;
  anio: number;
  placa: string;
  color: string;
  fotoUrl: string | null;
  createdAt: string;
  updatedAt: string;
}

/** Payload para crear/actualizar un auto. */
export interface CarInput {
  marca: string;
  modelo: string;
  anio: number;
  placa: string;
  color: string;
  fotoUrl?: string | null;
}

/** Respuesta paginada de `GET /cars`. */
export interface CarListResponse {
  content: Car[];
  page: number;
  size: number;
  total: number;
}

/** Filtros + paginación aceptados por `GET /cars`. */
export interface CarSearchParams {
  placa?: string;
  modelo?: string;
  marca?: string;
  anio?: number;
  page?: number;
  size?: number;
  sort?: string;
}

/**
 * Cliente tipado de `cars-service` (puerto 8082). Paralelo de
 * `src/api/carsApi.ts` en la versión React.
 *
 * <p>No añade cabeceras de autorización aquí: eso es responsabilidad del
 * `authInterceptor` — de esa forma un test puede ejercitar este servicio
 * sin tener que mockear el storage del token.</p>
 */
@Injectable({ providedIn: 'root' })
export class CarsApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.carsUrl;

  list(params: CarSearchParams = {}): Observable<CarListResponse> {
    let httpParams = new HttpParams();
    // Serializamos sólo los valores definidos para no mandar `placa=` vacíos
    // que ensuciarían el log de Spring MVC.
    (Object.keys(params) as (keyof CarSearchParams)[]).forEach((k) => {
      const v = params[k];
      if (v !== undefined && v !== null && v !== '') {
        httpParams = httpParams.set(k, String(v));
      }
    });
    return this.http.get<CarListResponse>(`${this.baseUrl}/cars`, { params: httpParams });
  }

  create(input: CarInput): Observable<Car> {
    return this.http.post<Car>(`${this.baseUrl}/cars`, input);
  }

  update(id: string, input: CarInput): Observable<Car> {
    return this.http.put<Car>(`${this.baseUrl}/cars/${id}`, input);
  }

  remove(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/cars/${id}`);
  }
}
