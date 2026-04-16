import {
  DataGrid,
  type GridColDef,
  type GridPaginationModel,
  type GridRenderCellParams,
} from '@mui/x-data-grid';
import { Avatar, Box, Chip, IconButton, Stack, Tooltip, Typography } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import DirectionsCarFilledIcon from '@mui/icons-material/DirectionsCarFilled';
import type { Car } from '../api/carsApi';

interface Props {
  rows: Car[];
  total: number;
  loading: boolean;
  paginationModel: GridPaginationModel;
  onPaginationChange(model: GridPaginationModel): void;
  onEdit(car: Car): void;
  onDelete(car: Car): void;
}

export default function CarTable({
  rows, total, loading, paginationModel, onPaginationChange, onEdit, onDelete,
}: Props) {
  const columns: GridColDef<Car>[] = [
    {
      field: 'marca',
      headerName: 'Marca',
      flex: 1,
      minWidth: 160,
      renderCell: ({ value }: GridRenderCellParams<Car>) => (
        <Stack
          direction="row"
          spacing={1.5}
          alignItems="center"
          sx={{ height: '100%', width: '100%' }}
        >
          <Avatar
            variant="rounded"
            sx={{
              width: 36,
              height: 36,
              bgcolor: 'primary.light',
              color: 'primary.contrastText',
              flexShrink: 0,
            }}
          >
            <DirectionsCarFilledIcon fontSize="small" />
          </Avatar>
          <Typography
            variant="body2"
            fontWeight={600}
            noWrap
            sx={{ minWidth: 0 }}
          >
            {value}
          </Typography>
        </Stack>
      ),
    },
    {
      field: 'modelo',
      headerName: 'Modelo',
      flex: 1,
      minWidth: 130,
      renderCell: ({ value }: GridRenderCellParams<Car>) => (
        <Box sx={{ display: 'flex', alignItems: 'center', height: '100%' }}>
          <Typography variant="body2" color="text.secondary" noWrap>
            {value}
          </Typography>
        </Box>
      ),
    },
    {
      field: 'anio',
      headerName: 'Año',
      width: 90,
      align: 'center',
      headerAlign: 'center',
      renderCell: ({ value }: GridRenderCellParams<Car>) => (
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', width: '100%' }}>
          <Typography variant="body2">{value}</Typography>
        </Box>
      ),
    },
    {
      field: 'placa',
      headerName: 'Placa',
      width: 130,
      renderCell: ({ value }: GridRenderCellParams<Car>) => (
        <Box sx={{ display: 'flex', alignItems: 'center', height: '100%' }}>
          <Chip
            label={value}
            size="small"
            sx={{
              fontFamily: 'ui-monospace, SFMono-Regular, monospace',
              fontWeight: 600,
              bgcolor: 'grey.100',
              color: 'text.primary',
            }}
          />
        </Box>
      ),
    },
    {
      field: 'color',
      headerName: 'Color',
      flex: 1,
      minWidth: 110,
      renderCell: ({ value }: GridRenderCellParams<Car>) => (
        <Box sx={{ display: 'flex', alignItems: 'center', height: '100%' }}>
          <Typography variant="body2" noWrap>{value}</Typography>
        </Box>
      ),
    },
    {
      field: 'actions',
      headerName: '',
      width: 120,
      sortable: false,
      filterable: false,
      align: 'right',
      headerAlign: 'right',
      renderCell: (params: GridRenderCellParams<Car>) => (
        <Stack
          direction="row"
          spacing={0.5}
          justifyContent="flex-end"
          alignItems="center"
          sx={{ height: '100%', width: '100%' }}
        >
          <Tooltip title="Editar">
            <IconButton
              onClick={() => onEdit(params.row as Car)}
              size="small"
              sx={{ color: 'primary.main' }}
            >
              <EditIcon fontSize="small" />
            </IconButton>
          </Tooltip>
          <Tooltip title="Eliminar">
            <IconButton
              onClick={() => onDelete(params.row as Car)}
              size="small"
              sx={{ color: 'error.main' }}
            >
              <DeleteIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </Stack>
      ),
    },
  ];

  return (
    <DataGrid
      autoHeight
      rows={rows}
      columns={columns}
      loading={loading}
      rowCount={total}
      paginationMode="server"
      paginationModel={paginationModel}
      onPaginationModelChange={onPaginationChange}
      pageSizeOptions={[5, 10, 20, 50]}
      disableRowSelectionOnClick
      rowHeight={68}
      columnHeaderHeight={52}
      sx={{
        border: 0,
        '& .MuiDataGrid-row:hover': { bgcolor: 'action.hover' },
        '& .MuiDataGrid-columnHeaders': {
          borderBottom: '1px solid rgba(30, 58, 138, 0.08)',
        },
        '& .MuiDataGrid-columnHeader': {
          bgcolor: '#f9fafb',
        },
        '& .MuiDataGrid-cell': {
          display: 'flex',
          alignItems: 'center',
          py: 1,
        },
        '& .MuiDataGrid-cell:focus, & .MuiDataGrid-cell:focus-within': {
          outline: 'none',
        },
        '& .MuiDataGrid-columnHeader:focus, & .MuiDataGrid-columnHeader:focus-within': {
          outline: 'none',
        },
      }}
    />
  );
}
