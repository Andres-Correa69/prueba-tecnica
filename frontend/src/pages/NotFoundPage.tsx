import { Container, Typography, Button, Stack } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <Container maxWidth="sm" sx={{ mt: 10 }}>
      <Stack spacing={2} alignItems="center">
        <Typography variant="h3">404</Typography>
        <Typography>Ruta no encontrada.</Typography>
        <Button component={RouterLink} to="/" variant="contained">Volver al inicio</Button>
      </Stack>
    </Container>
  );
}
