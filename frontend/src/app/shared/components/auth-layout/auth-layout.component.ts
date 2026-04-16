import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

/**
 * Envolvente visual compartido por las pantallas de login y registro.
 * Portado de `src/components/AuthLayout.tsx`: centra verticalmente una
 * card sobre un fondo con gradiente diagonal y añade un encabezado
 * consistente (logo + título + subtítulo).
 *
 * El contenido real del formulario se proyecta con `<ng-content>`.
 */
@Component({
  selector: 'app-auth-layout',
  standalone: true,
  imports: [MatIconModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="auth-layout">
      <section class="auth-card">
        <header class="auth-card__header">
          <div class="auth-card__logo">
            <mat-icon fontIcon="directions_car"></mat-icon>
          </div>
          <div>
            <p class="auth-card__brand">UFINET AUTOS</p>
            <p class="auth-card__brand-sub">Gestión de vehículos</p>
          </div>
        </header>

        <div>
          <h1 class="auth-card__title">{{ title }}</h1>
          <p class="auth-card__subtitle">{{ subtitle }}</p>
        </div>

        <ng-content></ng-content>

        @if (footer) {
          <p class="auth-card__footer">{{ footer }}</p>
        }
      </section>
    </div>
  `,
  styles: [`
    :host { display: block; }

    .auth-layout {
      min-height: 100dvh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 32px 16px;
      background:
        radial-gradient(1200px 600px at 10% -10%, rgba(245, 158, 11, 0.12), transparent 60%),
        radial-gradient(1000px 700px at 110% 10%, rgba(59, 91, 191, 0.22), transparent 55%),
        linear-gradient(135deg, #0f1d4a 0%, #1e3a8a 55%, #3b5bbf 100%);
    }

    .auth-card {
      width: 100%;
      max-width: 440px;
      background: #ffffff;
      border-radius: 16px;
      padding: 40px;
      display: flex;
      flex-direction: column;
      gap: 24px;
      box-shadow:
        0 20px 50px rgba(15, 23, 42, 0.35),
        0 8px 20px rgba(15, 23, 42, 0.2);

      @media (max-width: 600px) {
        padding: 24px;
      }
    }

    .auth-card__header {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .auth-card__logo {
      width: 48px;
      height: 48px;
      border-radius: 10px;
      display: grid;
      place-items: center;
      color: #ffffff;
      background: linear-gradient(135deg, #1e3a8a 0%, #3b5bbf 100%);
    }

    .auth-card__brand {
      margin: 0;
      font-weight: 600;
      color: #1e3a8a;
      letter-spacing: 1px;
      font-size: 12px;
    }

    .auth-card__brand-sub {
      margin: 0;
      font-size: 12px;
      color: #6b7280;
    }

    .auth-card__title {
      margin: 0 0 8px 0;
      font-weight: 700;
      letter-spacing: -0.01em;
      font-size: 28px;
      color: #1f2937;
    }

    .auth-card__subtitle {
      margin: 0;
      color: #6b7280;
      font-size: 14px;
    }

    .auth-card__footer {
      margin: 0;
      text-align: center;
      color: #6b7280;
      font-size: 14px;
    }
  `],
})
export class AuthLayoutComponent {
  @Input({ required: true }) title!: string;
  @Input({ required: true }) subtitle!: string;
  @Input() footer?: string;
}
