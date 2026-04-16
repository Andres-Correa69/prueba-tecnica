import { AbstractControl, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';

/**
 * Validadores compartidos entre los formularios de login, registro y
 * creación/edición de autos. Portados uno a uno desde los esquemas Zod
 * que vivían en `src/schemas/*.ts` en la versión React.
 *
 * <p>Por qué validadores manuales en vez de un port de Zod: Angular
 * `Validators` habla el mismo lenguaje que los `FormControl` y se
 * compone con `[formGroup]` y `[errorStateMatcher]` sin capa extra. El
 * objetivo aquí es que los mensajes de error sean idénticos a los de
 * la versión React (los memoricé para la prueba) y que la validación
 * sea granular (un error por campo, no un objeto fusionado).</p>
 */

const USERNAME_REGEX = /^[a-zA-Z0-9._-]+$/;
const PLACA_REGEX = /^[A-Z0-9]{3}-?[A-Z0-9]{3}$/;

export const usernameValidators: ValidatorFn[] = [
  Validators.required,
  Validators.minLength(3),
  Validators.maxLength(30),
  Validators.pattern(USERNAME_REGEX),
];

/** Contraseña de LOGIN: sólo exige que exista. No rechequeamos reglas
 * porque si el usuario ya se registró alguna vez con política más laxa
 * no tiene sentido frenarlo aquí. */
export const loginPasswordValidators: ValidatorFn[] = [Validators.required];

/** Contraseña de REGISTRO: 8-100 chars, obligatoriamente letras + dígitos. */
export const registerPasswordValidators: ValidatorFn[] = [
  Validators.required,
  Validators.minLength(8),
  Validators.maxLength(100),
  passwordHasLettersAndDigits(),
];

export function passwordHasLettersAndDigits(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const v = control.value ?? '';
    if (!v) return null;
    const ok = /[A-Za-z]/.test(v) && /\d/.test(v);
    return ok ? null : { passwordComposition: true };
  };
}

export const placaValidators: ValidatorFn[] = [
  Validators.required,
  Validators.pattern(PLACA_REGEX),
];

export const marcaValidators: ValidatorFn[] = [
  Validators.required,
  Validators.maxLength(50),
];

export const modeloValidators: ValidatorFn[] = [
  Validators.required,
  Validators.maxLength(50),
];

export const colorValidators: ValidatorFn[] = [
  Validators.required,
  Validators.maxLength(30),
];

export function anioValidators(): ValidatorFn[] {
  const currentYear = new Date().getFullYear();
  return [
    Validators.required,
    Validators.min(1900),
    Validators.max(currentYear + 1),
    integerOnly(),
  ];
}

export function integerOnly(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const v = control.value;
    if (v === null || v === undefined || v === '') return null;
    const n = Number(v);
    if (!Number.isFinite(n) || !Number.isInteger(n)) return { integerOnly: true };
    return null;
  };
}

/** URL opcional — acepta vacío, pero si trae algo debe comenzar por
 * `http://` o `https://`. Mismo criterio que el esquema Zod original. */
export const fotoUrlValidators: ValidatorFn[] = [
  Validators.maxLength(500),
  optionalHttpUrl(),
];

export function optionalHttpUrl(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const v = (control.value ?? '').trim();
    if (!v) return null;
    return /^https?:\/\//i.test(v) ? null : { httpUrl: true };
  };
}

/**
 * Traduce los códigos de error emitidos por los validadores (y por
 * `Validators`) al español que se mostraba en la versión React. La vista
 * llama a `firstErrorMessage(control)` para obtener el texto final.
 */
export function firstErrorMessage(control: AbstractControl | null): string | null {
  if (!control || !control.errors || (!control.touched && !control.dirty)) return null;
  const e = control.errors;
  if (e['required']) return 'requerido';
  if (e['minlength']) return `mínimo ${e['minlength'].requiredLength} caracteres`;
  if (e['maxlength']) return `máximo ${e['maxlength'].requiredLength} caracteres`;
  if (e['pattern']) {
    // `pattern` no discrimina qué regex falló, así que el componente lo
    // reemplaza si tiene un mensaje más específico. Aquí damos un fallback.
    return 'formato inválido';
  }
  if (e['passwordComposition']) return 'debe incluir letras y dígitos';
  if (e['min']) return `mínimo ${e['min'].min}`;
  if (e['max']) return `máximo ${e['max'].max}`;
  if (e['integerOnly']) return 'sin decimales';
  if (e['httpUrl']) return 'debe empezar con http:// o https://';
  return 'valor inválido';
}
