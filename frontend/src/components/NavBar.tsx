import { useState } from 'react';
import {
  AppBar, Avatar, Box, Button, Divider, Menu, MenuItem,
  Stack, Toolbar, Tooltip, Typography,
} from '@mui/material';
import DirectionsCarFilledIcon from '@mui/icons-material/DirectionsCarFilled';
import LogoutIcon from '@mui/icons-material/Logout';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

/**
 * NavBar superior.
 *
 * - Muestra el logo + nombre del producto a la izquierda.
 * - A la derecha, menú del usuario (avatar con iniciales) con logout.
 * - En móviles se contrae el nombre del usuario, queda solo el avatar.
 */
export default function NavBar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [anchor, setAnchor] = useState<HTMLElement | null>(null);

  const initials = (user?.username ?? 'U').slice(0, 2).toUpperCase();

  const handleLogout = () => {
    setAnchor(null);
    logout();
    navigate('/login', { replace: true });
  };

  return (
    <AppBar position="sticky" color="inherit" elevation={0} sx={{ bgcolor: 'background.paper' }}>
      <Toolbar sx={{ gap: 1.5 }}>
        <Box
          sx={{
            width: 40,
            height: 40,
            borderRadius: 2,
            display: 'grid',
            placeItems: 'center',
            color: 'primary.contrastText',
            background: 'linear-gradient(135deg, #1e3a8a 0%, #3b5bbf 100%)',
          }}
        >
          <DirectionsCarFilledIcon fontSize="small" />
        </Box>
        <Stack spacing={0} sx={{ flexGrow: 1, minWidth: 0 }}>
          <Typography variant="h6" sx={{ lineHeight: 1.1 }}>
            Ufinet Autos
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Gestión de vehículos
          </Typography>
        </Stack>

        {user && (
          <>
            <Tooltip title={user.username}>
              <Button
                onClick={(e) => setAnchor(e.currentTarget)}
                color="inherit"
                sx={{
                  gap: 1,
                  textTransform: 'none',
                  px: 1,
                  '&:hover': { bgcolor: 'action.hover' },
                }}
              >
                <Avatar
                  sx={{
                    width: 36, height: 36,
                    bgcolor: 'primary.main',
                    fontSize: 14, fontWeight: 700,
                  }}
                >
                  {initials}
                </Avatar>
                <Typography
                  variant="body2"
                  sx={{ display: { xs: 'none', sm: 'block' }, fontWeight: 500 }}
                >
                  {user.username}
                </Typography>
              </Button>
            </Tooltip>
            <Menu
              anchorEl={anchor}
              open={Boolean(anchor)}
              onClose={() => setAnchor(null)}
              anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
              transformOrigin={{ vertical: 'top', horizontal: 'right' }}
              slotProps={{ paper: { sx: { mt: 1, minWidth: 200 } } }}
            >
              <Box sx={{ px: 2, py: 1 }}>
                <Typography variant="caption" color="text.secondary">
                  Sesión iniciada como
                </Typography>
                <Typography variant="body2" fontWeight={600}>
                  {user.username}
                </Typography>
              </Box>
              <Divider />
              <MenuItem onClick={handleLogout} sx={{ gap: 1, color: 'error.main' }}>
                <LogoutIcon fontSize="small" />
                Cerrar sesión
              </MenuItem>
            </Menu>
          </>
        )}
      </Toolbar>
    </AppBar>
  );
}
