import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import {
  Alert, Box, Button, InputAdornment, Link, Stack, TextField,
} from '@mui/material';
import PersonAddAlt1Icon from '@mui/icons-material/PersonAddAlt1';
import PersonOutlineIcon from '@mui/icons-material/PersonOutline';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import AuthLayout from '../components/AuthLayout';
import { registerFormSchema, type RegisterFormValues } from '../schemas/authSchemas';
import { useAuth } from '../auth/AuthContext';

export default function RegisterPage() {
  const { register: registerUser } = useAuth();
  const navigate = useNavigate();
  const [serverError, setServerError] = useState<string | null>(null);

  const { register, handleSubmit, formState } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerFormSchema),
    defaultValues: { username: '', password: '' },
  });

  const onSubmit = handleSubmit(async (values) => {
    setServerError(null);
    try {
      await registerUser(values.username, values.password);
      navigate('/cars', { replace: true });
    } catch (e: any) {
      const status = e?.response?.status;
      if (status === 409) setServerError('Ese usuario ya existe');
      else setServerError(e?.response?.data?.message ?? 'No se pudo registrar');
    }
  });

  return (
    <AuthLayout
      title="Crear cuenta"
      subtitle="Regístrate para empezar a administrar tus autos"
      footer={
        <>
          ¿Ya tienes cuenta?{' '}
          <Link component={RouterLink} to="/login" fontWeight={600}>
            Inicia sesión
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
            helperText={formState.errors.username?.message ?? 'Entre 3 y 30 caracteres'}
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
            autoComplete="new-password"
            {...register('password')}
            error={Boolean(formState.errors.password)}
            helperText={formState.errors.password?.message ?? 'Mínimo 8 caracteres, letras + dígitos'}
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
            startIcon={<PersonAddAlt1Icon />}
            sx={{ mt: 1 }}
          >
            {formState.isSubmitting ? 'Creando…' : 'Crear cuenta'}
          </Button>
        </Stack>
      </Box>
    </AuthLayout>
  );
}
