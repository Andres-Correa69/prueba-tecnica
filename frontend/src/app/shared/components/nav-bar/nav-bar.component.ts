import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';

import { AuthService } from '../../../core/auth/auth.service';

/**
 * Barra superior persistente para las páginas privadas (`/cars`).
 * Portada de `src/components/NavBar.tsx`: logo + nombre del producto
 * a la izquierda, menú de usuario (avatar con iniciales + logout) a la
 * derecha. En móviles se oculta el nombre del usuario.
 */
@Component({
  selector: 'app-nav-bar',
  standalone: true,
  imports: [
    MatToolbarModule, MatButtonModule, MatIconModule,
    MatMenuModule, MatTooltipModule, MatDividerModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <mat-toolbar class="nav-bar">
      <div class="nav-bar__brand">
        <div class="nav-bar__logo">
          <mat-icon fontIcon="directions_car" inline></mat-icon>
        </div>
        <div class="nav-bar__titles">
          <span class="nav-bar__product">Ufinet Autos</span>
          <span class="nav-bar__subtitle">Gestión de vehículos</span>
        </div>
      </div>

      <span class="nav-bar__spacer"></span>

      @if (user(); as u) {
        <button
          mat-button
          [matMenuTriggerFor]="menu"
          [matTooltip]="u.username"
          class="nav-bar__user-btn"
        >
          <span class="nav-bar__avatar">{{ initials() }}</span>
          <span class="nav-bar__username">{{ u.username }}</span>
        </button>

        <mat-menu #menu="matMenu" xPosition="before">
          <div class="nav-bar__menu-header">
            <span class="nav-bar__menu-caption">Sesión iniciada como</span>
            <strong>{{ u.username }}</strong>
          </div>
          <mat-divider></mat-divider>
          <button mat-menu-item class="nav-bar__logout" (click)="logout()">
            <mat-icon fontIcon="logout"></mat-icon>
            <span>Cerrar sesión</span>
          </button>
        </mat-menu>
      }
    </mat-toolbar>
  `,
  styles: [`
    .nav-bar {
      background: #ffffff;
      color: #1f2937;
      box-shadow: 0 1px 3px rgba(15, 23, 42, 0.08);
      position: sticky;
      top: 0;
      z-index: 10;
      gap: 12px;
    }

    .nav-bar__brand {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .nav-bar__logo {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      display: grid;
      place-items: center;
      color: #ffffff;
      background: linear-gradient(135deg, #1e3a8a 0%, #3b5bbf 100%);

      mat-icon { font-size: 22px; width: 22px; height: 22px; }
    }

    .nav-bar__titles {
      display: flex;
      flex-direction: column;
      line-height: 1.1;
    }

    .nav-bar__product {
      font-weight: 600;
      font-size: 18px;
    }

    .nav-bar__subtitle {
      font-size: 12px;
      color: #6b7280;
    }

    .nav-bar__spacer { flex: 1 1 auto; }

    .nav-bar__user-btn {
      gap: 8px;
      text-transform: none;
      padding: 0 12px;
    }

    .nav-bar__avatar {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: #1e3a8a;
      color: #ffffff;
      display: grid;
      place-items: center;
      font-weight: 700;
      font-size: 14px;
    }

    .nav-bar__username {
      font-weight: 500;
      @media (max-width: 600px) { display: none; }
    }

    .nav-bar__menu-header {
      padding: 12px 16px 8px;
      display: flex;
      flex-direction: column;
      min-width: 200px;
    }

    .nav-bar__menu-caption {
      font-size: 12px;
      color: #6b7280;
    }

    .nav-bar__logout {
      color: #ef4444;
    }
  `],
})
export class NavBarComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly user = this.auth.user;
  readonly initials = computed(() => (this.user()?.username ?? 'U').slice(0, 2).toUpperCase());

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login'], { replaceUrl: true });
  }
}
