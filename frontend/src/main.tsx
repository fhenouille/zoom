import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ConfigProvider } from 'antd';
import frFR from 'antd/locale/fr_FR';
import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import { AuthProvider } from './contexts/AuthContext';
import { setupGlobalErrorHandling } from './utils/errorHandler';
import './index.css';

// Initialize error handling
setupGlobalErrorHandling();
console.log('🚀 Zoom Meetings App Starting...');
console.log('📍 Base URL:', globalThis.location?.href);
console.log('📍 API Base URL:', import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api');

// Determine the basename for React Router based on the environment
const getBasename = () => {
  const isDev = !import.meta.env.PROD;
  return isDev ? '/' : '/zoom/';
};

const basename = getBasename();
console.log('📍 Router Basename:', basename);

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
      staleTime: 5 * 60 * 1000, // 5 minutes
    },
  },
});

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <ConfigProvider locale={frFR}>
        <BrowserRouter basename={basename}>
          <AuthProvider>
            <App />
          </AuthProvider>
        </BrowserRouter>
      </ConfigProvider>
    </QueryClientProvider>
  </React.StrictMode>
);
