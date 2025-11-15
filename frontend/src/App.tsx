import { Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import ProtectedRoute from './components/ProtectedRoute';
import Home from './pages/Home';
import Login from './pages/Login';
import Meetings from './pages/Meetings';

function App() {
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
