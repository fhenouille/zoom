import { AdminPanel } from '@/components/AdminPanel';
import { useAuth } from '@/contexts/AuthContext';
import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './Admin.css';

export function Admin() {
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    // Rediriger si l'utilisateur n'est pas ADMIN
    if (user && user.role !== 'ADMIN') {
      navigate('/');
    }
  }, [user, navigate]);

  if (!user || user.role !== 'ADMIN') {
    return (
      <div className="admin-page">
        <div className="access-denied">
          <h2>❌ Accès Refusé</h2>
          <p>Seuls les administrateurs peuvent accéder à cette page.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="admin-page">
      <AdminPanel />
    </div>
  );
}
