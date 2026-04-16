import { useEffect, useState } from 'react';
import { InputAdornment, TextField } from '@mui/material';
import Grid from '@mui/material/Grid2';
import SearchIcon from '@mui/icons-material/Search';
import DirectionsCarFilledIcon from '@mui/icons-material/DirectionsCarFilled';
import LocalOfferIcon from '@mui/icons-material/LocalOffer';
import EventIcon from '@mui/icons-material/Event';
import type { CarSearchParams } from '../api/carsApi';

interface Props {
  value: CarSearchParams;
  onChange(next: CarSearchParams): void;
}

/**
 * Barra de búsqueda con debounce.
 *
 * <p>Guardamos "lo que el usuario está escribiendo" en estado local y solo
 * empujamos al padre después de 350 ms de inactividad. Así evitamos
 * golpear el backend en cada tecla.</p>
 */
export default function SearchFilters({ value, onChange }: Props) {
  const [placa, setPlaca] = useState(value.placa ?? '');
  const [modelo, setModelo] = useState(value.modelo ?? '');
  const [marca, setMarca] = useState(value.marca ?? '');
  const [anio, setAnio] = useState<string>(value.anio ? String(value.anio) : '');

  useEffect(() => {
    const h = window.setTimeout(() => {
      onChange({
        ...value,
        placa: placa || undefined,
        modelo: modelo || undefined,
        marca: marca || undefined,
        anio: anio ? Number(anio) : undefined,
        page: 0,
      });
    }, 350);
    return () => window.clearTimeout(h);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [placa, modelo, marca, anio]);

  return (
    <Grid container spacing={2}>
      <Grid size={{ xs: 12, sm: 6, md: 3 }}>
        <TextField
          fullWidth
          size="small"
          label="Placa"
          value={placa}
          onChange={(e) => setPlaca(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon fontSize="small" color="action" />
              </InputAdornment>
            ),
          }}
        />
      </Grid>
      <Grid size={{ xs: 12, sm: 6, md: 3 }}>
        <TextField
          fullWidth
          size="small"
          label="Modelo"
          value={modelo}
          onChange={(e) => setModelo(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <DirectionsCarFilledIcon fontSize="small" color="action" />
              </InputAdornment>
            ),
          }}
        />
      </Grid>
      <Grid size={{ xs: 12, sm: 6, md: 3 }}>
        <TextField
          fullWidth
          size="small"
          label="Marca"
          value={marca}
          onChange={(e) => setMarca(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <LocalOfferIcon fontSize="small" color="action" />
              </InputAdornment>
            ),
          }}
        />
      </Grid>
      <Grid size={{ xs: 12, sm: 6, md: 3 }}>
        <TextField
          fullWidth
          size="small"
          label="Año"
          type="number"
          value={anio}
          onChange={(e) => setAnio(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <EventIcon fontSize="small" color="action" />
              </InputAdornment>
            ),
          }}
        />
      </Grid>
    </Grid>
  );
}
