import {
  ChangeDetectionStrategy, Component, DestroyRef, EventEmitter,
  Input, OnInit, Output, inject,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { debounceTime, distinctUntilChanged, map } from 'rxjs';

import { CarSearchParams } from '../../../core/api/cars-api.service';

/**
 * Barra de filtros (placa / modelo / marca / año) con **debounce de 350 ms**.
 *
 * Portada 1-a-1 de `SearchFilters.tsx`. Lo interesante en Angular:
 *
 *   - Usamos Reactive Forms (`FormGroup`) con `valueChanges` en lugar de
 *     `useState` + `useEffect`. Angular ya emite un observable al tipear,
 *     así que el debounce se hace con `debounceTime(350)` de RxJS en vez
 *     de con `setTimeout` manual.
 *   - Cada vez que cambian los filtros reseteamos `page` a 0, tal y como
 *     hacía la versión React — nunca queremos devolver al usuario a la
 *     página 5 del resultado anterior cuando acaba de filtrar por placa.
 *   - `takeUntilDestroyed()` asegura que el observable se desuscribe al
 *     destruirse el componente, evitando fugas de memoria.
 */
@Component({
  selector: 'app-search-filters',
  standalone: true,
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatIconModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <form [formGroup]="form" class="filters" (submit)="$event.preventDefault()">
      <mat-form-field appearance="outline">
        <mat-label>Placa</mat-label>
        <input matInput formControlName="placa" placeholder="ABC123" />
        <mat-icon matIconPrefix fontIcon="search"></mat-icon>
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Modelo</mat-label>
        <input matInput formControlName="modelo" placeholder="Corolla" />
        <mat-icon matIconPrefix fontIcon="directions_car"></mat-icon>
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Marca</mat-label>
        <input matInput formControlName="marca" placeholder="Toyota" />
        <mat-icon matIconPrefix fontIcon="local_offer"></mat-icon>
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Año</mat-label>
        <input matInput type="number" formControlName="anio" placeholder="2022" />
        <mat-icon matIconPrefix fontIcon="event"></mat-icon>
      </mat-form-field>
    </form>
  `,
  styles: [`
    .filters {
      display: grid;
      grid-template-columns: repeat(4, minmax(0, 1fr));
      gap: 16px;

      @media (max-width: 900px) { grid-template-columns: repeat(2, minmax(0, 1fr)); }
      @media (max-width: 600px) { grid-template-columns: 1fr; }
    }
  `],
})
export class SearchFiltersComponent implements OnInit {
  @Input() value: CarSearchParams = {};
  @Output() valueChange = new EventEmitter<CarSearchParams>();

  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  readonly form = this.fb.group({
    placa: [''],
    modelo: [''],
    marca: [''],
    anio: [null as number | null],
  });

  ngOnInit(): void {
    this.form.patchValue({
      placa: this.value.placa ?? '',
      modelo: this.value.modelo ?? '',
      marca: this.value.marca ?? '',
      anio: this.value.anio ?? null,
    }, { emitEvent: false });

    this.form.valueChanges
      .pipe(
        debounceTime(350),
        map((v) => ({
          placa: (v.placa ?? '').trim() || undefined,
          modelo: (v.modelo ?? '').trim() || undefined,
          marca: (v.marca ?? '').trim() || undefined,
          anio: v.anio != null && !Number.isNaN(Number(v.anio)) ? Number(v.anio) : undefined,
        })),
        distinctUntilChanged((a, b) =>
          a.placa === b.placa && a.modelo === b.modelo &&
          a.marca === b.marca && a.anio === b.anio),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((filters) => {
        // Al cambiar cualquier filtro, siempre volvemos a la primera página.
        this.valueChange.emit({ ...this.value, ...filters, page: 0 });
      });
  }
}
