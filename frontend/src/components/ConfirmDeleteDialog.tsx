import {
  Box, Button, Dialog, DialogActions, DialogContent,
  DialogContentText, DialogTitle, Stack, Typography,
} from '@mui/material';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import DeleteIcon from '@mui/icons-material/Delete';

interface Props {
  open: boolean;
  title: string;
  message: string;
  onCancel(): void;
  onConfirm(): void;
}

export default function ConfirmDeleteDialog({ open, title, message, onCancel, onConfirm }: Props) {
  return (
    <Dialog
      open={open}
      onClose={onCancel}
      maxWidth="xs"
      fullWidth
      slotProps={{ paper: { sx: { borderRadius: 3 } } }}
    >
      <DialogTitle sx={{ pt: 3 }}>
        <Stack direction="row" spacing={2} alignItems="center">
          <Box
            sx={{
              width: 44, height: 44, borderRadius: '50%',
              display: 'grid', placeItems: 'center',
              bgcolor: 'error.main', color: 'common.white',
            }}
          >
            <WarningAmberIcon />
          </Box>
          <Typography variant="h6">{title}</Typography>
        </Stack>
      </DialogTitle>
      <DialogContent>
        <DialogContentText>{message}</DialogContentText>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 3, gap: 1 }}>
        <Button onClick={onCancel} variant="text" color="inherit">Cancelar</Button>
        <Button
          color="error"
          variant="contained"
          onClick={onConfirm}
          startIcon={<DeleteIcon />}
        >
          Eliminar
        </Button>
      </DialogActions>
    </Dialog>
  );
}
