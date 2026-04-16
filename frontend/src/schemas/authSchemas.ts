import { z } from 'zod';

// Reflejamos las reglas del backend — defensa en profundidad, y además feedback inmediato para el usuario.
export const usernameSchema = z
  .string()
  .trim()
  .min(3, 'mínimo 3 caracteres')
  .max(30, 'máximo 30 caracteres')
  .regex(/^[a-zA-Z0-9._-]+$/, 'solo letras, dígitos y . _ -');

export const passwordSchema = z
  .string()
  .min(8, 'mínimo 8 caracteres')
  .max(100, 'máximo 100 caracteres')
  .refine((v) => /[A-Za-z]/.test(v) && /\d/.test(v), 'debe incluir letras y dígitos');

export const loginFormSchema = z.object({
  username: usernameSchema,
  password: z.string().min(1, 'requerido'),
});

export const registerFormSchema = z.object({
  username: usernameSchema,
  password: passwordSchema,
});

export type LoginFormValues = z.infer<typeof loginFormSchema>;
export type RegisterFormValues = z.infer<typeof registerFormSchema>;
