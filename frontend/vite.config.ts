import react from '@vitejs/plugin-react';
import path from 'path';
import { defineConfig, loadEnv } from 'vite';

// https://vitejs.dev/config/
// Production build uses API endpoint from environment variables (Railway backend)
export default defineConfig(({ command, mode }) => {
  // Load env vars based on mode
  const env = loadEnv(mode, process.cwd(), '');

  return {
    base: command === 'build' ? '/zoom/' : '/',
    define: {
      __API_BASE_URL__: JSON.stringify(env.VITE_API_BASE_URL || 'http://localhost:8080/api'),
    },
    plugins: [react()],
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
  };
});
