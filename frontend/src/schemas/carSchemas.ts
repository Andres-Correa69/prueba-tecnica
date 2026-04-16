import { z } from 'zod';

const currentYear = new Date().getFullYear();

export const carFormSchema = z.object({
  marca: z.string().trim().min(1, 'requerido').max(50),
  modelo: z.string().trim().min(1, 'requerido').max(50),
  anio: z.coerce
    .number({ invalid_type_error: 'debe ser número' })
    .int('sin decimales')
    .min(1900, 'mínimo 1900')
    .max(currentYear + 1, `máximo ${currentYear + 1}`),
  placa: z
    .string()
    .trim()
    .toUpperCase()
    .regex(/^[A-Z0-9]{3}-?[A-Z0-9]{3}$/, 'formato de placa inválido'),
  color: z.string().trim().min(1, 'requerido').max(30),
  fotoUrl: z
    .string()
    .trim()
    .max(500)
    .optional()
    .or(z.literal(''))
    .transform((v) => (v && v.length > 0 ? v : undefined))
    .refine(
      (v) => v === undefined || /^https?:\/\//i.test(v),
      'debe empezar con http:// o https://'
    ),
});

export type CarFormValues = z.infer<typeof carFormSchema>;
