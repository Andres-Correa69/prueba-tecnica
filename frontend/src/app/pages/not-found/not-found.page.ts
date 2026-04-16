import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';

/**
 * Pantalla 404. Portada de `src/pages/NotFoundPage.tsx`.
 * La ruta catch-all del router apunta aquí.
 */
@Component({
  selector: 'app-not-found-page',
  standalone: true,
  imports: [RouterLink, MatButtonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="not-found">
      <div class="not-found__card">
        <p class="not-found__eyebrow">404</p>
        <h1 class="not-found__title">Ruta no encontrada</h1>
        <p class="not-found__lead">
          La página que buscas no existe o fue movida.
        </p>
        <a mat-flat-button color="primary" routerLink="/">Volver al inicio</a>
      </div>
    </div>
  `,
  styles: [`
    .not-found {
      min-height: 100dvh;
      display: grid;
      place-items: center;
      padding: 24px;
      background: var(--app-bg);
    }

    .not-found__card {
      background: #ffffff;
      border-radius: 16px;
      padding: 40px;
      max-width: 420px;
      text-align: center;
      border: 1px solid rgba(30, 58, 138, 0.06);
      box-shadow: var(--app-card-shadow);
    }

    .not-found__eyebrow {
      color: #1e3a8a;
      font-weight: 700;
      letter-spacing: 2px;
      margin: 0 0 8px;
    }

    .not-found__title {
      margin: 0 0 8px;
      font-weight: 700;
    }

    .not-found__lead {
      color: #6b7280;
      margin: 0 0 24px;
    }
  `],
})
export class NotFoundPage {}
