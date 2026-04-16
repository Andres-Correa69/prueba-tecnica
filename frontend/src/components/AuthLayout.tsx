import type { ReactNode } from 'react';
import { Box, Stack, Typography } from '@mui/material';
import DirectionsCarFilledIcon from '@mui/icons-material/DirectionsCarFilled';

interface Props {
  title: string;
  subtitle: string;
  children: ReactNode;
  footer?: ReactNode;
}

/**
 * Layout compartido entre la pantalla de login y la de registro.
 *
 * <p>Centra verticalmente una card sobre un fondo con gradiente diagonal
 * y añade un encabezado consistente (logo + título + subtítulo). Extraerlo
 * evita duplicar 40 líneas de marcado en cada página.</p>
 */
export default function AuthLayout({ title, subtitle, children, footer }: Props) {
  return (
    <Box
      sx={{
        minHeight: '100dvh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        px: 2,
        py: 4,
        background:
          'radial-gradient(1200px 600px at 10% -10%, rgba(245, 158, 11, 0.12), transparent 60%),' +
          'radial-gradient(1000px 700px at 110% 10%, rgba(59, 91, 191, 0.22), transparent 55%),' +
          'linear-gradient(135deg, #0f1d4a 0%, #1e3a8a 55%, #3b5bbf 100%)',
      }}
    >
      <Box
        sx={{
          width: '100%',
          maxWidth: 440,
          bgcolor: 'background.paper',
          borderRadius: 3,
          p: { xs: 3, sm: 5 },
          boxShadow:
            '0 20px 50px rgba(15, 23, 42, 0.35), 0 8px 20px rgba(15, 23, 42, 0.2)',
        }}
      >
        <Stack spacing={3}>
          <Stack direction="row" spacing={1.5} alignItems="center">
            <Box
              sx={{
                width: 48,
                height: 48,
                borderRadius: 2,
                display: 'grid',
                placeItems: 'center',
                color: 'primary.contrastText',
                background:
                  'linear-gradient(135deg, #1e3a8a 0%, #3b5bbf 100%)',
              }}
            >
              <DirectionsCarFilledIcon />
            </Box>
            <Box>
              <Typography variant="subtitle2" color="primary.main" sx={{ letterSpacing: 1, textTransform: 'uppercase' }}>
                Ufinet Autos
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Gestión de vehículos
              </Typography>
            </Box>
          </Stack>

          <Box>
            <Typography variant="h4" gutterBottom>{title}</Typography>
            <Typography variant="body2" color="text.secondary">{subtitle}</Typography>
          </Box>

          {children}

          {footer && (
            <Typography variant="body2" color="text.secondary" textAlign="center">
              {footer}
            </Typography>
          )}
        </Stack>
      </Box>
    </Box>
  );
}
