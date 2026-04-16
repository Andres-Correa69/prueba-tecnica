import { createTheme } from '@mui/material/styles';

/**
 * Tema MUI centralizado.
 *
 * Mantener aquí la paleta y la tipografía (en vez de esparcirlas en props sx)
 * nos permite rebrandear cambiando un solo archivo.
 *
 * Decisiones visuales:
 *   - Paleta: azul profundo + acento ámbar → aire "enterprise moderno".
 *   - Fondo gris muy suave para que los Paper/Card resalten sin contraste agresivo.
 *   - borderRadius generoso (12 px) para un look amigable.
 *   - Sombras muy sutiles (el default de MUI es demasiado pesado).
 *   - Tipografía del sistema con pesos definidos por jerarquía.
 */
export const theme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#1e3a8a',
      light: '#3b5bbf',
      dark: '#0f1d4a',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#f59e0b',
      light: '#fbbf24',
      dark: '#b45309',
      contrastText: '#1f2937',
    },
    background: {
      default: '#f5f7fb',
      paper: '#ffffff',
    },
    text: {
      primary: '#1f2937',
      secondary: '#6b7280',
    },
    divider: 'rgba(30, 58, 138, 0.08)',
    success: { main: '#10b981' },
    error:   { main: '#ef4444' },
    warning: { main: '#f59e0b' },
    info:    { main: '#3b82f6' },
  },
  shape: {
    borderRadius: 12,
  },
  typography: {
    fontFamily: '"Inter", "Segoe UI", system-ui, -apple-system, sans-serif',
    h1: { fontWeight: 700, letterSpacing: '-0.02em' },
    h2: { fontWeight: 700, letterSpacing: '-0.02em' },
    h3: { fontWeight: 700, letterSpacing: '-0.01em' },
    h4: { fontWeight: 700, letterSpacing: '-0.01em' },
    h5: { fontWeight: 600 },
    h6: { fontWeight: 600 },
    button: { textTransform: 'none', fontWeight: 600, letterSpacing: 0 },
  },
  components: {
    MuiPaper: {
      styleOverrides: {
        root: { backgroundImage: 'none' },
      },
    },
    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: {
        root: {
          borderRadius: 10,
          paddingInline: 20,
          paddingBlock: 10,
        },
        containedPrimary: {
          boxShadow: '0 4px 14px rgba(30, 58, 138, 0.25)',
          '&:hover': { boxShadow: '0 6px 20px rgba(30, 58, 138, 0.35)' },
        },
      },
    },
    MuiTextField: {
      defaultProps: { variant: 'outlined', size: 'medium' },
    },
    MuiOutlinedInput: {
      styleOverrides: { root: { borderRadius: 10 } },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          boxShadow: '0 1px 3px rgba(15, 23, 42, 0.04), 0 6px 24px rgba(15, 23, 42, 0.04)',
          border: '1px solid rgba(30, 58, 138, 0.06)',
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: { boxShadow: '0 1px 3px rgba(15, 23, 42, 0.08)' },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        head: {
          fontWeight: 600,
          color: '#374151',
          backgroundColor: '#f9fafb',
        },
      },
    },
  },
});
