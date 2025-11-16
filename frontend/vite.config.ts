import react from '@vitejs/plugin-react';
import path from 'path';
import { defineConfig } from 'vite';

// https://vitejs.dev/config/
// Production build uses API endpoint injected by build process
const API_BASE_URL = process.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

export default defineConfig(({ command }) => ({
  base: command === 'build' ? '/zoom/' : '/',
  plugins: [react()],
  define: {
    __API_BASE_URL__: JSON.stringify(API_BASE_URL),
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/tests/setup.ts',
  },
}));
