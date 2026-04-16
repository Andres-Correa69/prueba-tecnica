import { ChangeDetectionStrategy, Component, Inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import {
  MAT_DIALOG_DATA, MatDialogModule, MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { Car, CarInput, CarsApiService } from '../../../core/api/cars-api.service';
import {
  anioValidators, colorValidators, firstErrorMessage, fotoUrlValidators,
  marcaValidators, modeloValidators, placaValidators,
} from '../../validators/form.validators';

/** Payload que la página pasa al abrir el diálogo. `car` nulo ↔ modo crear. */
export interface CarFormData {
  car: Car | null;
}

/**
 * Dialog para crear o editar un auto. Portado de `CarFormDialog.tsx`.
 *
 * <p>Diferencias clave vs. la versión React:
 *   - `react-hook-form` + `zodResolver` se sustituye por **Reactive Forms**
 *     + validadores compuestos en `shared/validators/form.validators.ts`.
 *   - El diálogo llama al backend desde aquí porque necesita mostrar 409
 *     inline. Si todo sale bien cierra devolviendo el `Car` resultante,
 *     que la página usa para refrescar la tabla.</p>
 */
@Component({
  selector: 'app-car-form-dialog',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule,
    MatInputModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <header class="car-form__header">
      <h2 mat-dialog-title>{{ isEdit ? 'Editar auto' : 'Nuevo auto' }}</h2>
      <p class="car-form__subtitle">
        {{ isEdit ? 'Actualiza los datos del vehículo' : 'Registra un vehículo en tu flota' }}
      </p>
    </header>

    <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
      <mat-dialog-content>
        <div class="car-form__grid">
          <mat-form-field appearance="outline">
            <mat-label>Marca</mat-label>
            <input matInput formControlName="marca" maxlength="50" required />
            <mat-icon matIconPrefix fontIcon="local_offer"></mat-icon>
            @if (errorFor('marca'); as e) { <mat-error>{{ e }}</mat-error> }
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Modelo</mat-label>
            <input matInput formControlName="modelo" maxlength="50" required />
            @if (errorFor('modelo'); as e) { <mat-error>{{ e }}</mat-error> }
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Año</mat-label>
            <input matInput type="number" formControlName="anio" required />
            <mat-icon matIconPrefix fontIcon="event"></mat-icon>
            @if (errorFor('anio'); as e) { <mat-error>{{ e }}</mat-error> }
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Placa</mat-label>
            <input matInput formControlName="placa" placeholder="ABC123" required />
            <mat-icon matIconPrefix fontIcon="badge"></mat-icon>
            @if (errorFor('placa'); as e) {
              <mat-error>{{ e }}</mat-error>
            } @else {
              <mat-hint>Formato: ABC123 o ABC-123</mat-hint>
            }
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Color</mat-label>
            <input matInput formControlName="color" maxlength="30" required />
            <mat-icon matIconPrefix fontIcon="palette"></mat-icon>
            @if (errorFor('color'); as e) { <mat-error>{{ e }}</mat-error> }
          </mat-form-field>

          <mat-form-field appearance="outline" class="car-form__wide">
            <mat-label>URL de foto (opcional)</mat-label>
            <input matInput formControlName="fotoUrl" maxlength="500" placeholder="https://..." />
            <mat-icon matIconPrefix fontIcon="link"></mat-icon>
            @if (errorFor('fotoUrl'); as e) { <mat-error>{{ e }}</mat-error> }
          </mat-form-field>
        </div>

        @if (submitError()) {
          <div class="car-form__alert" role="alert">{{ submitError() }}</div>
        }
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-button type="button" (click)="cancel()" [disabled]="saving()">Cancelar</button>
        <button
          mat-flat-button
          color="primary"
          type="submit"
          [disabled]="saving() || form.invalid"
        >
          @if (saving()) {
            <mat-spinner diameter="18"></mat-spinner>
          } @else {
            {{ isEdit ? 'Guardar cambios' : 'Crear auto' }}
          }
        </button>
      </mat-dialog-actions>
    </form>
  `,
  styles: [`
    .car-form__header { padding: 24px 24px 0; }
    h2[mat-dialog-title] { margin: 0; }
    .car-form__subtitle {
      margin: 4px 0 0;
      color: #6b7280;
      font-size: 14px;
    }

    .car-form__grid {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 16px;
      padding-top: 8px;
      @media (max-width: 600px) { grid-template-columns: 1fr; }
    }
    .car-form__wide { grid-column: 1 / -1; }

    .car-form__alert {
      margin-top: 16px;
      padding: 12px 16px;
      border-radius: 10px;
      background: rgba(239, 68, 68, 0.08);
      color: #b91c1c;
      font-size: 14px;
    }
  `],
})
export class CarFormDialogComponent {
  readonly isEdit: boolean;
  private readonly editingId: string | null;

  readonly saving = signal(false);
  readonly submitError = signal<string | null>(null);

  readonly form = this.fb.group({
    marca:   ['', { validators: marcaValidators }],
    modelo:  ['', { validators: modeloValidators }],
    anio:    [new Date().getFullYear(), { validators: anioValidators() }],
    placa:   ['', { validators: placaValidators }],
    color:   ['', { validators: colorValidators }],
    fotoUrl: ['', { validators: fotoUrlValidators }],
  });

  constructor(
    private readonly ref: MatDialogRef<CarFormDialogComponent, Car | null>,
    @Inject(MAT_DIALOG_DATA) data: CarFormData,
    private readonly fb: FormBuilder,
    private readonly api: CarsApiService,
  ) {
    this.isEdit = data.car !== null;
    this.editingId = data.car?.id ?? null;
    if (data.car) {
      this.form.patchValue({
        marca: data.car.marca,
        modelo: data.car.modelo,
        anio: data.car.anio,
        placa: data.car.placa,
        color: data.car.color,
        fotoUrl: data.car.fotoUrl ?? '',
      });
    }
  }

  errorFor(controlName: keyof typeof this.form.controls): string | null {
    const c = this.form.controls[controlName];
    const base = firstErrorMessage(c);
    if (base === 'formato inválido' && controlName === 'placa') {
      return 'formato de placa inválido (ej. ABC123)';
    }
    return base;
  }

  cancel(): void { this.ref.close(null); }

  async submit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.submitError.set(null);

    const raw = this.form.getRawValue();
    const input: CarInput = {
      marca: (raw.marca ?? '').trim(),
      modelo: (raw.modelo ?? '').trim(),
      anio: Number(raw.anio),
      placa: (raw.placa ?? '').toUpperCase(),
      color: (raw.color ?? '').trim(),
      fotoUrl: (raw.fotoUrl ?? '').trim() || undefined,
    };

    try {
      const saved = this.editingId
        ? await firstValueFrom(this.api.update(this.editingId, input))
        : await firstValueFrom(this.api.create(input));
      this.ref.close(saved);
    } catch (err) {
      this.submitError.set(readErrorMessage(err, this.isEdit));
    } finally {
      this.saving.set(false);
    }
  }
}

function readErrorMessage(err: unknown, isEdit: boolean): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 409) return 'Ya existe un auto con esa placa';
    const body = err.error as { message?: string } | undefined;
    if (body?.message) return body.message;
  }
  return isEdit ? 'No se pudo guardar el auto' : 'No se pudo crear el auto';
}
