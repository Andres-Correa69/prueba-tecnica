import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { firstValueFrom } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { NavBarComponent } from '../../shared/components/nav-bar/nav-bar.component';
import { CarTableComponent } from '../../shared/components/car-table/car-table.component';
import {
  CarFormDialogComponent, CarFormData,
} from '../../shared/components/car-form-dialog/car-form-dialog.component';
import {
  ConfirmDeleteDialogComponent, ConfirmDeleteData,
} from '../../shared/components/confirm-delete-dialog/confirm-delete-dialog.component';
import { SearchFiltersComponent } from '../../shared/components/search-filters/search-filters.component';
import { AuthService } from '../../core/auth/auth.service';
import {
  Car, CarSearchParams, CarsApiService,
} from '../../core/api/cars-api.service';

/**
 * Dashboard principal del usuario autenticado. Portada de
 * `src/pages/CarsPage.tsx`. Responsabilidades:
 *   - Hero con saludo + botón "Nuevo auto".
 *   - Tarjetas de estadísticas (total, marcas distintas de la página
 *     actual, modelos recientes = año ≥ currentYear − 1).
 *   - Barra de filtros con debounce.
 *   - Tabla paginada del servidor.
 *   - Dialogs de crear/editar y de confirmar borrado.
 *
 * <p>En Angular todo el estado local vive en `signal`s: Angular recomputa
 * automáticamente los `computed` (por ejemplo `stats`) cuando cambian las
 * dependencias, sin necesidad de `useMemo`.</p>
 */
@Component({
  selector: 'app-cars-page',
  standalone: true,
  imports: [
    CommonModule, MatButtonModule, MatCardModule, MatDialogModule, MatIconModule,
    NavBarComponent, CarTableComponent, SearchFiltersComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="cars-page">
      <app-nav-bar></app-nav-bar>

      <div class="app-container">
        <div class="cars-page__hero">
          <div>
            <p class="cars-page__eyebrow">Panel principal</p>
            <h1 class="cars-page__greeting">
              Hola, {{ auth.user()?.username ?? '' }} 👋
            </h1>
            <p class="cars-page__lead">
              Aquí puedes ver, crear, editar y eliminar tus autos.
            </p>
          </div>
          <button mat-flat-button color="primary" class="cars-page__new" (click)="openCreate()">
            <mat-icon fontIcon="add"></mat-icon>
            Nuevo auto
          </button>
        </div>

        <div class="cars-page__stats">
          <mat-card class="stat-card">
            <mat-card-content>
              <div class="stat-card__icon stat-card__icon--primary">
                <mat-icon fontIcon="directions_car"></mat-icon>
              </div>
              <div>
                <div class="stat-card__label">Total de autos</div>
                <div class="stat-card__value">{{ total() }}</div>
              </div>
            </mat-card-content>
          </mat-card>

          <mat-card class="stat-card">
            <mat-card-content>
              <div class="stat-card__icon stat-card__icon--accent">
                <mat-icon fontIcon="local_offer"></mat-icon>
              </div>
              <div>
                <div class="stat-card__label">Marcas (página)</div>
                <div class="stat-card__value">{{ stats().marcas }}</div>
              </div>
            </mat-card-content>
          </mat-card>

          <mat-card class="stat-card">
            <mat-card-content>
              <div class="stat-card__icon stat-card__icon--success">
                <mat-icon fontIcon="event"></mat-icon>
              </div>
              <div>
                <div class="stat-card__label">Modelos recientes</div>
                <div class="stat-card__value">{{ stats().nuevos }}</div>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <section class="cars-page__filters">
          <h2 class="cars-page__section-title">Filtros de búsqueda</h2>
          <app-search-filters
            [value]="search()"
            (valueChange)="onFiltersChange($event)"
          ></app-search-filters>
        </section>

        @if (error()) {
          <div class="cars-page__alert" role="alert">
            {{ error() }}
            <button mat-icon-button (click)="error.set(null)" aria-label="Cerrar">
              <mat-icon fontIcon="close"></mat-icon>
            </button>
          </div>
        }

        <section class="cars-page__table">
          <app-car-table
            [rows]="rows()"
            [total]="total()"
            [loading]="loading()"
            [pageIndex]="pageIndex()"
            [pageSize]="pageSize()"
            (pageChange)="onPageChange($event)"
            (edit)="openEdit($event)"
            (delete)="askDelete($event)"
          ></app-car-table>
        </section>
      </div>
    </div>
  `,
  styles: [`
    .cars-page { min-height: 100dvh; background: var(--app-bg); }

    .cars-page__hero {
      display: flex;
      flex-direction: column;
      gap: 16px;
      margin-bottom: 32px;

      @media (min-width: 900px) {
        flex-direction: row;
        align-items: center;
        justify-content: space-between;
      }
    }

    .cars-page__eyebrow {
      margin: 0 0 4px;
      color: #1e3a8a;
      font-size: 12px;
      font-weight: 600;
      letter-spacing: 1px;
      text-transform: uppercase;
    }

    .cars-page__greeting {
      margin: 0 0 8px;
      font-size: 32px;
      font-weight: 700;
      letter-spacing: -0.01em;
    }

    .cars-page__lead {
      margin: 0;
      color: #6b7280;
    }

    .cars-page__new {
      height: 48px;
      align-self: flex-start;
    }

    .cars-page__stats {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 16px;
      margin-bottom: 32px;
      @media (max-width: 900px) { grid-template-columns: 1fr; }
    }

    .stat-card mat-card-content {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 16px !important;
    }

    .stat-card__icon {
      width: 48px;
      height: 48px;
      border-radius: 10px;
      display: grid;
      place-items: center;
    }
    .stat-card__icon--primary { background: rgba(30, 58, 138, 0.08); color: #1e3a8a; }
    .stat-card__icon--accent  { background: rgba(245, 158, 11, 0.08); color: #f59e0b; }
    .stat-card__icon--success { background: rgba(16, 185, 129, 0.08); color: #10b981; }

    .stat-card__label { font-size: 12px; color: #6b7280; }
    .stat-card__value { font-size: 24px; font-weight: 600; }

    .cars-page__filters,
    .cars-page__table {
      background: #ffffff;
      border: 1px solid rgba(30, 58, 138, 0.06);
      border-radius: 12px;
      padding: 20px;
      margin-bottom: 24px;
      box-shadow: var(--app-card-shadow);
    }

    .cars-page__table { padding: 0; overflow: hidden; }

    .cars-page__section-title {
      margin: 0 0 16px;
      color: #6b7280;
      font-size: 13px;
      font-weight: 500;
      text-transform: uppercase;
      letter-spacing: 0.6px;
    }

    .cars-page__alert {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
      padding: 12px 16px;
      border-radius: 10px;
      background: rgba(239, 68, 68, 0.08);
      color: #b91c1c;
      font-size: 14px;
      margin-bottom: 16px;
    }
  `],
})
export class CarsPage {
  readonly auth = inject(AuthService);
  private readonly api = inject(CarsApiService);
  private readonly dialog = inject(MatDialog);

  readonly rows = signal<Car[]>([]);
  readonly total = signal(0);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly search = signal<CarSearchParams>({ page: 0, size: 10 });
  readonly pageIndex = computed(() => this.search().page ?? 0);
  readonly pageSize = computed(() => this.search().size ?? 10);

  readonly stats = computed(() => {
    const thisYear = new Date().getFullYear();
    const r = this.rows();
    return {
      marcas: new Set(r.map((c) => c.marca.toLowerCase())).size,
      nuevos: r.filter((c) => c.anio >= thisYear - 1).length,
    };
  });

  constructor() {
    this.reload();
  }

  async reload(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);
    try {
      const data = await firstValueFrom(this.api.list(this.search()));
      this.rows.set(data.content);
      this.total.set(data.total);
    } catch (err) {
      this.error.set(readLoadError(err));
    } finally {
      this.loading.set(false);
    }
  }

  onFiltersChange(next: CarSearchParams): void {
    this.search.set({ ...next, size: this.pageSize() });
    void this.reload();
  }

  onPageChange(event: { pageIndex: number; pageSize: number }): void {
    this.search.update((s) => ({ ...s, page: event.pageIndex, size: event.pageSize }));
    void this.reload();
  }

  openCreate(): void {
    this.openForm({ car: null });
  }

  openEdit(car: Car): void {
    this.openForm({ car });
  }

  private openForm(data: CarFormData): void {
    const ref = this.dialog.open<
      CarFormDialogComponent, CarFormData, Car | null
    >(CarFormDialogComponent, { data, width: '640px', autoFocus: 'first-tabbable' });
    ref.afterClosed().subscribe((saved) => {
      if (saved) void this.reload();
    });
  }

  askDelete(car: Car): void {
    const data: ConfirmDeleteData = {
      title: 'Eliminar auto',
      message:
        `¿Seguro que quieres eliminar ${car.marca} ${car.modelo} (${car.placa})? ` +
        `Esta acción no se puede deshacer.`,
    };
    const ref = this.dialog.open<
      ConfirmDeleteDialogComponent, ConfirmDeleteData, boolean
    >(ConfirmDeleteDialogComponent, { data, width: '420px' });
    ref.afterClosed().subscribe(async (confirmed) => {
      if (!confirmed) return;
      try {
        await firstValueFrom(this.api.remove(car.id));
        await this.reload();
      } catch (err) {
        this.error.set(readDeleteError(err));
      }
    });
  }
}

function readLoadError(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    const body = err.error as { message?: string } | undefined;
    if (body?.message) return body.message;
  }
  return 'No se pudieron cargar los autos';
}

function readDeleteError(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    const body = err.error as { message?: string } | undefined;
    if (body?.message) return body.message;
  }
  return 'No se pudo eliminar el auto';
}
