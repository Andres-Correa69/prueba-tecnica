import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import {
  MAT_DIALOG_DATA, MatDialogModule, MatDialogRef,
} from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface ConfirmDeleteData {
  title: string;
  message: string;
}

/**
 * Dialog modal de confirmación antes de borrar un auto.
 *
 * <p>Portado de `ConfirmDeleteDialog.tsx`. En Material 17 los diálogos se
 * abren con `MatDialog.open(ComponentClass, { data })` y el componente
 * recibe el payload por `@Inject(MAT_DIALOG_DATA)`. El resultado se
 * propaga cerrando el diálogo con `close(true | false)`, que la página
 * que lo abrió recibe como un `Observable` vía `afterClosed()`.</p>
 */
@Component({
  selector: 'app-confirm-delete-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="confirm-dialog">
      <header class="confirm-dialog__header">
        <div class="confirm-dialog__icon">
          <mat-icon fontIcon="warning_amber"></mat-icon>
        </div>
        <h2 mat-dialog-title class="confirm-dialog__title">{{ data.title }}</h2>
      </header>

      <mat-dialog-content>
        <p class="confirm-dialog__msg">{{ data.message }}</p>
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-button (click)="cancel()">Cancelar</button>
        <button mat-flat-button color="warn" (click)="confirm()">
          <mat-icon fontIcon="delete"></mat-icon>
          Eliminar
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .confirm-dialog { max-width: 420px; }

    .confirm-dialog__header {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 24px 24px 0;
    }

    .confirm-dialog__icon {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: rgba(239, 68, 68, 0.1);
      color: #ef4444;
      display: grid;
      place-items: center;
    }

    .confirm-dialog__title {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
    }

    .confirm-dialog__msg {
      color: #4b5563;
      margin: 8px 0 0 0;
      line-height: 1.5;
    }
  `],
})
export class ConfirmDeleteDialogComponent {
  constructor(
    private readonly ref: MatDialogRef<ConfirmDeleteDialogComponent, boolean>,
    @Inject(MAT_DIALOG_DATA) public readonly data: ConfirmDeleteData,
  ) {}

  cancel(): void { this.ref.close(false); }
  confirm(): void { this.ref.close(true); }
}
