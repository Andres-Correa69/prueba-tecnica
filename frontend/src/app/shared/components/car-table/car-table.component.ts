import {
  ChangeDetectionStrategy, Component, EventEmitter,
  Input, Output,
} from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';

import { Car } from '../../../core/api/cars-api.service';

/**
 * Tabla de autos. Versión Angular Material de `CarTable.tsx`.
 *
 * Usa `mat-table` + `mat-paginator` en modo **server-side**: el componente
 * NO ordena ni pagina localmente, sólo emite eventos hacia arriba y es
 * la página quien repide al backend. El motivo es idéntico al del
 * DataGrid de MUI: el volumen de autos puede crecer y no queremos
 * traer todo a memoria.
 */
@Component({
  selector: 'app-car-table',
  standalone: true,
  imports: [
    MatTableModule, MatPaginatorModule, MatButtonModule, MatIconModule,
    MatTooltipModule, MatProgressBarModule, MatChipsModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (loading) { <mat-progress-bar mode="indeterminate"></mat-progress-bar> }

    <div class="car-table__scroll">
      <table mat-table [dataSource]="rows" class="car-table">
        <ng-container matColumnDef="marca">
          <th mat-header-cell *matHeaderCellDef>Marca</th>
          <td mat-cell *matCellDef="let car">
            <div class="car-table__brand">
              <span class="car-table__avatar">{{ initialsOf(car.marca) }}</span>
              <span>{{ car.marca }}</span>
            </div>
          </td>
        </ng-container>

        <ng-container matColumnDef="modelo">
          <th mat-header-cell *matHeaderCellDef>Modelo</th>
          <td mat-cell *matCellDef="let car">{{ car.modelo }}</td>
        </ng-container>

        <ng-container matColumnDef="anio">
          <th mat-header-cell *matHeaderCellDef>Año</th>
          <td mat-cell *matCellDef="let car">{{ car.anio }}</td>
        </ng-container>

        <ng-container matColumnDef="placa">
          <th mat-header-cell *matHeaderCellDef>Placa</th>
          <td mat-cell *matCellDef="let car">
            <span class="car-table__plate">{{ car.placa }}</span>
          </td>
        </ng-container>

        <ng-container matColumnDef="color">
          <th mat-header-cell *matHeaderCellDef>Color</th>
          <td mat-cell *matCellDef="let car">{{ car.color }}</td>
        </ng-container>

        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef class="car-table__actions-cell">Acciones</th>
          <td mat-cell *matCellDef="let car" class="car-table__actions-cell">
            <button
              mat-icon-button
              matTooltip="Editar"
              (click)="edit.emit(car)"
              aria-label="Editar auto"
            >
              <mat-icon fontIcon="edit"></mat-icon>
            </button>
            <button
              mat-icon-button
              matTooltip="Eliminar"
              color="warn"
              (click)="delete.emit(car)"
              aria-label="Eliminar auto"
            >
              <mat-icon fontIcon="delete"></mat-icon>
            </button>
          </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="columns"></tr>
        <tr mat-row *matRowDef="let row; columns: columns;"></tr>

        <tr class="mat-row" *matNoDataRow>
          <td [attr.colspan]="columns.length" class="car-table__empty">
            No hay autos que mostrar con los filtros actuales.
          </td>
        </tr>
      </table>
    </div>

    <mat-paginator
      [length]="total"
      [pageIndex]="pageIndex"
      [pageSize]="pageSize"
      [pageSizeOptions]="[5, 10, 20, 50]"
      (page)="onPage($event)"
    ></mat-paginator>
  `,
  styles: [`
    .car-table__scroll { overflow-x: auto; }
    .car-table { width: 100%; background: transparent; }
    .car-table td, .car-table th { padding: 12px 16px; height: 68px; }

    .car-table__brand {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .car-table__avatar {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      display: grid;
      place-items: center;
      font-weight: 700;
      font-size: 13px;
      background: #eef1fb;
      color: #1e3a8a;
    }

    .car-table__plate {
      font-family: 'Menlo', 'Consolas', monospace;
      font-size: 13px;
      font-weight: 600;
      background: #eef1fb;
      color: #1e3a8a;
      padding: 4px 10px;
      border-radius: 999px;
      letter-spacing: 0.5px;
    }

    .car-table__actions-cell {
      width: 120px;
      text-align: right;
    }

    .car-table__empty {
      padding: 40px 16px;
      text-align: center;
      color: #6b7280;
    }
  `],
})
export class CarTableComponent {
  @Input() rows: Car[] = [];
  @Input() total = 0;
  @Input() loading = false;
  @Input() pageIndex = 0;
  @Input() pageSize = 10;

  @Output() pageChange = new EventEmitter<{ pageIndex: number; pageSize: number }>();
  @Output() edit = new EventEmitter<Car>();
  @Output() delete = new EventEmitter<Car>();

  readonly columns = ['marca', 'modelo', 'anio', 'placa', 'color', 'actions'];

  onPage(event: PageEvent): void {
    this.pageChange.emit({ pageIndex: event.pageIndex, pageSize: event.pageSize });
  }

  initialsOf(marca: string): string {
    return (marca ?? '').slice(0, 2).toUpperCase();
  }
}
