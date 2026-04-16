import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AuthLayoutComponent } from '../../shared/components/auth-layout/auth-layout.component';
import { AuthService } from '../../core/auth/auth.service';
import {
  firstErrorMessage, registerPasswordValidators, usernameValidators,
} from '../../shared/validators/form.validators';

/**
 * Pantalla de registro. Portada de `src/pages/RegisterPage.tsx`.
 *
 * Tras un registro exitoso el `AuthService.register()` hace auto-login,
 * así que aquí sólo redirigimos a `/cars` cuando termina.
 */
@Component({
  selector: 'app-register-page',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    MatFormFieldModule, MatInputModule, MatButtonModule,
    MatIconModule, MatProgressSpinnerModule,
    AuthLayoutComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-auth-layout
      title="Crea tu cuenta"
      subtitle="Regístrate para administrar tus autos"
    >
      <form [formGroup]="form" (ngSubmit)="submit()" novalidate class="auth-form">
        <mat-form-field appearance="outline">
          <mat-label>Usuario</mat-label>
          <input matInput formControlName="username" autocomplete="username" required />
          <mat-icon matIconPrefix fontIcon="person"></mat-icon>
          @if (errorFor('username'); as e) {
            <mat-error>{{ e }}</mat-error>
          } @else {
            <mat-hint>3-30 caracteres, letras/dígitos/ . _ -</mat-hint>
          }
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Contraseña</mat-label>
          <input
            matInput
            [type]="showPassword() ? 'text' : 'password'"
            formControlName="password"
            autocomplete="new-password"
            required
          />
          <mat-icon matIconPrefix fontIcon="lock"></mat-icon>
          <button
            mat-icon-button
            matSuffix
            type="button"
            (click)="showPassword.set(!showPassword())"
            [attr.aria-label]="showPassword() ? 'Ocultar contraseña' : 'Mostrar contraseña'"
          >
            <mat-icon [fontIcon]="showPassword() ? 'visibility_off' : 'visibility'"></mat-icon>
          </button>
          @if (errorFor('password'); as e) {
            <mat-error>{{ e }}</mat-error>
          } @else {
            <mat-hint>Mínimo 8 caracteres, con letras y dígitos</mat-hint>
          }
        </mat-form-field>

        @if (submitError()) {
          <div class="auth-form__alert" role="alert">{{ submitError() }}</div>
        }

        <button
          mat-flat-button
          color="primary"
          type="submit"
          class="auth-form__submit"
          [disabled]="submitting()"
        >
          @if (submitting()) {
            <mat-spinner diameter="18"></mat-spinner>
          } @else {
            Crear cuenta
          }
        </button>

        <p class="auth-form__alt">
          ¿Ya tienes cuenta?
          <a routerLink="/login">Inicia sesión</a>
        </p>
      </form>
    </app-auth-layout>
  `,
  styles: [`
    .auth-form {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }
    .auth-form__submit { height: 48px; }
    .auth-form__alt {
      text-align: center;
      margin: 8px 0 0;
      color: #6b7280;
      font-size: 14px;
      a { color: #1e3a8a; font-weight: 600; text-decoration: none; }
      a:hover { text-decoration: underline; }
    }
    .auth-form__alert {
      padding: 12px 16px;
      border-radius: 10px;
      background: rgba(239, 68, 68, 0.08);
      color: #b91c1c;
      font-size: 14px;
    }
  `],
})
export class RegisterPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly submitting = signal(false);
  readonly submitError = signal<string | null>(null);
  readonly showPassword = signal(false);

  readonly form = this.fb.group({
    username: ['', { validators: usernameValidators }],
    password: ['', { validators: registerPasswordValidators }],
  });

  errorFor(controlName: keyof typeof this.form.controls): string | null {
    return firstErrorMessage(this.form.controls[controlName]);
  }

  async submit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.submitError.set(null);

    const { username, password } = this.form.getRawValue();
    try {
      await this.auth.register((username ?? '').trim(), password ?? '');
      await this.router.navigateByUrl('/cars', { replaceUrl: true });
    } catch (err) {
      this.submitError.set(readRegisterError(err));
    } finally {
      this.submitting.set(false);
    }
  }
}

function readRegisterError(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 409) return 'Ese usuario ya existe';
    const body = err.error as { message?: string } | undefined;
    if (body?.message) return body.message;
  }
  return 'No se pudo registrar';
}
