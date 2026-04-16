import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Elegimos Vite sobre CRA porque:
//   - el dev server arranca en ~150ms vs varios segundos en CRA,
//   - ESM nativo / HMR rápido,
//   - CRA ya no se recomienda oficialmente.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true,
  },
});
