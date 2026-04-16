import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link as RouterLink, useLocation, useNavigate } from 'react-router-dom';
import {
  Alert, Box, Button, InputAdornment, Link, Stack, TextField,
} from '@mui/material';
import PersonOutlineIcon from '@mui/icons-material/PersonOutline';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import LoginIcon from '@mui/icons-material/Login';
import AuthLayout from '../components/AuthLayout';
import { loginFormSchema, type LoginFormValues } from '../schemas/authSchemas';
import { useAuth } from '../auth/AuthContext';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: Location } | null)?.from?.pathname ?? '/cars';
  const [serverError, setServerError] = useState<string | null>(null);

  const { register, handleSubmit, formState } = useForm<LoginFormValues>({
    resolver: zodResolver(loginFormSchema),
    defaultValues: { username: '', password: '' },
  });

  const onSubmit = handleSubmit(async (values) => {
    setServerError(null);
    try {
      await login(values.username, values.password);
      navigate(from, { replace: true });
    } catch (e: any) {
      const status = e?.response?.status;
      if (status === 401) setServerError('Usuario o contraseña incorrectos');
      else setServerError(e?.response?.data?.message ?? 'No se pudo iniciar sesión');
    }
  });

  return (
    <AuthLayout
      title="Bienvenido de nuevo"
      subtitle="Ingresa tus credenciales para continuar"
      footer={
        <>
          ¿No tienes cuenta?{' '}
          <Link component={RouterLink} to="/register" fontWeight={600}>
            Regístrate aquí
          </Link>
        </>
      }
    >
      <Box component="form" onSubmit={onSubmit} noValidate>
        <Stack spacing={2.5}>
          <TextField
            label="Usuario"
            autoFocus
            fullWidth
            autoComplete="username"
            {...register('username')}
            error={Boolean(formState.errors.username)}
            helperText={formState.errors.username?.message ?? ' '}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <PersonOutlineIcon color="action" />
                </InputAdornment>
              ),
            }}
          />
          <TextField
            label="Contraseña"
            type="password"
            fullWidth
            autoComplete="current-password"
            {...register('password')}
            error={Boolean(formState.errors.password)}
            helperText={formState.errors.password?.message ?? ' '}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <LockOutlinedIcon color="action" />
                </InputAdornment>
              ),
            }}
          />

          {serverError && <Alert severity="error">{serverError}</Alert>}

          <Button
            type="submit"
            variant="contained"
            size="large"
            fullWidth
            disabled={formState.isSubmitting}
            startIcon={<LoginIcon />}
            sx={{ mt: 1 }}
          >
            {formState.isSubmitting ? 'Ingresando…' : 'Ingresar'}
          </Button>
        </Stack>
      </Box>
    </AuthLayout>
  );
}
