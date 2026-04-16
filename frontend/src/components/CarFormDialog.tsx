import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle,
  IconButton, InputAdornment, Stack, TextField, Typography,
} from '@mui/material';
import Grid from '@mui/material/Grid2';
import CloseIcon from '@mui/icons-material/Close';
import DirectionsCarFilledIcon from '@mui/icons-material/DirectionsCarFilled';
import LocalOfferIcon from '@mui/icons-material/LocalOffer';
import EventIcon from '@mui/icons-material/Event';
import BadgeIcon from '@mui/icons-material/Badge';
import PaletteIcon from '@mui/icons-material/Palette';
import LinkIcon from '@mui/icons-material/Link';
import SaveOutlinedIcon from '@mui/icons-material/SaveOutlined';
import AddIcon from '@mui/icons-material/Add';
import { carFormSchema, type CarFormValues } from '../schemas/carSchemas';
import type { Car, CarInput } from '../api/carsApi';

interface Props {
  open: boolean;
  initial?: Car | null;
  onClose(): void;
  onSubmit(values: CarInput): Promise<void>;
}

/**
 * Modal de formulario usado TANTO para crear como para editar.
 *
 * <p>Un único diálogo (con prop {@code initial}) evita duplicar el layout y el
 * esquema de validación en dos componentes. Los campos están agrupados
 * visualmente: datos del vehículo arriba (marca/modelo/año), datos de
 * identificación abajo (placa/color/foto).</p>
 */
export default function CarFormDialog({ open, initial, onClose, onSubmit }: Props) {
  const [serverError, setServerError] = useState<string | null>(null);

  const { register, handleSubmit, reset, formState } = useForm<CarFormValues>({
    resolver: zodResolver(carFormSchema),
    defaultValues: emptyForm(),
  });

  useEffect(() => {
    if (open) {
      setServerError(null);
      reset(initial ? {
        marca: initial.marca,
        modelo: initial.modelo,
        anio: initial.anio,
        placa: initial.placa,
        color: initial.color,
        fotoUrl: initial.fotoUrl ?? '',
      } : emptyForm());
    }
  }, [open, initial, reset]);

  const submit = handleSubmit(async (values) => {
    setServerError(null);
    try {
      await onSubmit({ ...values, fotoUrl: values.fotoUrl ?? null });
      onClose();
    } catch (e: any) {
      const status = e?.response?.status;
      if (status === 409) setServerError('Ya existe un auto con esa placa');
      else setServerError(e?.response?.data?.message ?? 'No se pudo guardar');
    }
  });

  const isEdit = Boolean(initial);

  return (
    <Dialog
      open={open}
      onClose={onClose}
      fullWidth
      maxWidth="sm"
      slotProps={{ paper: { sx: { borderRadius: 3 } } }}
    >
      <DialogTitle sx={{ pr: 6, pt: 3 }}>
        <Stack direction="row" alignItems="center" spacing={1.5}>
          <Box
            sx={{
              width: 40, height: 40, borderRadius: 2,
              display: 'grid', placeItems: 'center',
              bgcolor: 'primary.main', color: 'primary.contrastText',
            }}
          >
            <DirectionsCarFilledIcon fontSize="small" />
          </Box>
          <Box>
            <Typography variant="h6" sx={{ lineHeight: 1.2 }}>
              {isEdit ? 'Editar auto' : 'Nuevo auto'}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {isEdit ? 'Actualiza los datos del vehículo' : 'Registra un vehículo en tu flota'}
            </Typography>
          </Box>
        </Stack>
        <IconButton
          onClick={onClose}
          sx={{ position: 'absolute', top: 12, right: 12 }}
          aria-label="Cerrar"
        >
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <form onSubmit={submit} noValidate>
        <DialogContent sx={{ pt: 1 }}>
          <Grid container spacing={{ xs: 2, sm: 3 }}>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Marca"
                placeholder="Toyota"
                {...register('marca')}
                error={!!formState.errors.marca}
                helperText={formState.errors.marca?.message ?? ' '}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <LocalOfferIcon color="action" fontSize="small" />
                    </InputAdornment>
                  ),
                }}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Modelo"
                placeholder="Corolla"
                {...register('modelo')}
                error={!!formState.errors.modelo}
                helperText={formState.errors.modelo?.message ?? ' '}
              />
            </Grid>

            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Año"
                type="number"
                placeholder="2020"
                {...register('anio')}
                error={!!formState.errors.anio}
                helperText={formState.errors.anio?.message ?? ' '}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <EventIcon color="action" fontSize="small" />
                    </InputAdornment>
                  ),
                }}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Placa"
                placeholder="ABC123"
                {...register('placa')}
                error={!!formState.errors.placa}
                helperText={formState.errors.placa?.message ?? 'Letras y dígitos, ej. ABC123'}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <BadgeIcon color="action" fontSize="small" />
                    </InputAdornment>
                  ),
                }}
              />
            </Grid>

            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Color"
                placeholder="Rojo"
                {...register('color')}
                error={!!formState.errors.color}
                helperText={formState.errors.color?.message ?? ' '}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <PaletteIcon color="action" fontSize="small" />
                    </InputAdornment>
                  ),
                }}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                fullWidth
                label="Foto URL (opcional)"
                placeholder="https://…"
                {...register('fotoUrl')}
                error={!!formState.errors.fotoUrl}
                helperText={formState.errors.fotoUrl?.message ?? ' '}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <LinkIcon color="action" fontSize="small" />
                    </InputAdornment>
                  ),
                }}
              />
            </Grid>

            {serverError && (
              <Grid size={{ xs: 12 }}>
                <Alert severity="error">{serverError}</Alert>
              </Grid>
            )}
          </Grid>
        </DialogContent>

        <DialogActions sx={{ px: 3, pb: 3, pt: 1, gap: 1 }}>
          <Button onClick={onClose} variant="text" color="inherit">
            Cancelar
          </Button>
          <Button
            type="submit"
            variant="contained"
            size="large"
            disabled={formState.isSubmitting}
            startIcon={isEdit ? <SaveOutlinedIcon /> : <AddIcon />}
          >
            {formState.isSubmitting ? 'Guardando…' : (isEdit ? 'Guardar cambios' : 'Crear auto')}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}

function emptyForm(): CarFormValues {
  return { marca: '', modelo: '', anio: new Date().getFullYear(), placa: '', color: '', fotoUrl: undefined };
}
