import { useEffect } from 'react';
import { Route, Routes, useLocation } from 'react-router-dom';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import Home from './pages/Home';
import Login from './pages/Login';
import Meetings from './pages/Meetings';

function App() {
  const location = useLocation();
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

  useEffect(() => {
    console.log('📍 Current Route:', location.pathname);
    console.log('🚀 API Base URL:', apiBaseUrl);
    console.log('✅ Production build with Railway backend');
  }, [location.pathname, apiBaseUrl]);

  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route
        path="/"
        element={
          <Layout>
            <Home />
          </Layout>
        }
      />
      <Route
        path="/meetings"
        element={
          <ProtectedRoute>
            <Layout>
              <Meetings />
            </Layout>
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}

export default App;
