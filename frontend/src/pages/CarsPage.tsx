import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Alert, Box, Button, Card, CardContent, Container, Paper,
  Stack, Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DirectionsCarFilledIcon from '@mui/icons-material/DirectionsCarFilled';
import LocalOfferIcon from '@mui/icons-material/LocalOffer';
import EventIcon from '@mui/icons-material/Event';
import type { GridPaginationModel } from '@mui/x-data-grid';
import NavBar from '../components/NavBar';
import CarTable from '../components/CarTable';
import CarFormDialog from '../components/CarFormDialog';
import ConfirmDeleteDialog from '../components/ConfirmDeleteDialog';
import SearchFilters from '../components/SearchFilters';
import { useAuth } from '../auth/AuthContext';
import { carsApi, type Car, type CarInput, type CarSearchParams } from '../api/carsApi';

export default function CarsPage() {
  const { user } = useAuth();

  const [rows, setRows] = useState<Car[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [search, setSearch] = useState<CarSearchParams>({ page: 0, size: 10 });
  const [pagination, setPagination] = useState<GridPaginationModel>({ page: 0, pageSize: 10 });

  const [editing, setEditing] = useState<Car | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [toDelete, setToDelete] = useState<Car | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await carsApi.list({
        ...search,
        page: pagination.page,
        size: pagination.pageSize,
      });
      setRows(data.content);
      setTotal(data.total);
    } catch (e: any) {
      setError(e?.response?.data?.message ?? 'No se pudieron cargar los autos');
    } finally {
      setLoading(false);
    }
  }, [search, pagination.page, pagination.pageSize]);

  useEffect(() => { load(); }, [load]);

  const handleSubmit = async (input: CarInput) => {
    if (editing) await carsApi.update(editing.id, input);
    else await carsApi.create(input);
    await load();
  };

  const handleDelete = async () => {
    if (!toDelete) return;
    try {
      await carsApi.remove(toDelete.id);
      setToDelete(null);
      await load();
    } catch (e: any) {
      setError(e?.response?.data?.message ?? 'No se pudo eliminar');
    }
  };

  const stats = useMemo(() => {
    const thisYear = new Date().getFullYear();
    return {
      total,
      marcas: new Set(rows.map((c) => c.marca.toLowerCase())).size,
      nuevos: rows.filter((c) => c.anio >= thisYear - 1).length,
    };
  }, [rows, total]);

  return (
    <Box sx={{ bgcolor: 'background.default', minHeight: '100dvh' }}>
      <NavBar />

      <Container maxWidth="lg" sx={{ py: { xs: 3, md: 5 } }}>
        {/* Hero */}
        <Stack
          direction={{ xs: 'column', md: 'row' }}
          justifyContent="space-between"
          alignItems={{ xs: 'flex-start', md: 'center' }}
          spacing={2}
          mb={4}
        >
          <Box>
            <Typography variant="overline" color="primary.main">
              Panel principal
            </Typography>
            <Typography variant="h4" gutterBottom>
              Hola, {user?.username} 👋
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Aquí puedes ver, crear, editar y eliminar tus autos.
            </Typography>
          </Box>
          <Button
            variant="contained"
            size="large"
            startIcon={<AddIcon />}
            onClick={() => { setEditing(null); setFormOpen(true); }}
          >
            Nuevo auto
          </Button>
        </Stack>

        {/* Tarjetas de estadísticas */}
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} mb={4}>
          <StatCard
            icon={<DirectionsCarFilledIcon />}
            label="Total de autos"
            value={stats.total.toString()}
            color="#1e3a8a"
          />
          <StatCard
            icon={<LocalOfferIcon />}
            label="Marcas (página)"
            value={stats.marcas.toString()}
            color="#f59e0b"
          />
          <StatCard
            icon={<EventIcon />}
            label="Modelos recientes"
            value={stats.nuevos.toString()}
            color="#10b981"
          />
        </Stack>

        {/* Filtros */}
        <Paper variant="outlined" sx={{ p: { xs: 2, md: 3 }, mb: 3 }}>
          <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 2 }}>
            Filtros de búsqueda
          </Typography>
          <SearchFilters value={search} onChange={setSearch} />
        </Paper>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        {/* Tabla */}
        <Paper variant="outlined" sx={{ overflow: 'hidden' }}>
          <CarTable
            rows={rows}
            total={total}
            loading={loading}
            paginationModel={pagination}
            onPaginationChange={setPagination}
            onEdit={(car) => { setEditing(car); setFormOpen(true); }}
            onDelete={(car) => setToDelete(car)}
          />
        </Paper>

        <CarFormDialog
          open={formOpen}
          initial={editing}
          onClose={() => setFormOpen(false)}
          onSubmit={handleSubmit}
        />

        <ConfirmDeleteDialog
          open={Boolean(toDelete)}
          title="Eliminar auto"
          message={`¿Seguro que quieres eliminar ${toDelete?.marca ?? ''} ${toDelete?.modelo ?? ''} (${toDelete?.placa ?? ''})? Esta acción no se puede deshacer.`}
          onCancel={() => setToDelete(null)}
          onConfirm={handleDelete}
        />
      </Container>
    </Box>
  );
}

interface StatCardProps {
  icon: React.ReactNode;
  label: string;
  value: string;
  color: string;
}

function StatCard({ icon, label, value, color }: StatCardProps) {
  return (
    <Card sx={{ flex: 1 }}>
      <CardContent>
        <Stack direction="row" spacing={2} alignItems="center">
          <Box
            sx={{
              width: 48, height: 48,
              borderRadius: 2,
              display: 'grid', placeItems: 'center',
              bgcolor: `${color}15`,
              color,
            }}
          >
            {icon}
          </Box>
          <Box>
            <Typography variant="caption" color="text.secondary">
              {label}
            </Typography>
            <Typography variant="h5">{value}</Typography>
          </Box>
        </Stack>
      </CardContent>
    </Card>
  );
}
