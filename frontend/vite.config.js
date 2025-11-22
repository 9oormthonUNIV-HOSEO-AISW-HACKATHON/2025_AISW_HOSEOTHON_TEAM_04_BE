import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

const DEV_BACKEND = process.env.VITE_API_BASE_URL || 'http://localhost:3008';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3007,
    proxy: {
      '/api': {
        target: DEV_BACKEND,
        changeOrigin: true,
      },
    },
  },
});
