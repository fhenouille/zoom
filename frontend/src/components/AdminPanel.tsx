import { useEffect, useState } from 'react';
import {
  CreateUserRequest,
  UpdateUserRequest,
  UserResponse,
  userService,
} from '../services/userService';
import './AdminPanel.css';

interface FormState {
  username: string;
  password: string;
  role: string;
}

interface EditingUser {
  username: string;
  role: string;
}

export function AdminPanel() {
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingUser, setEditingUser] = useState<EditingUser | null>(null);
  const [formData, setFormData] = useState<FormState>({
    username: '',
    password: '',
    role: 'USER',
  });
  const [updatePassword, setUpdatePassword] = useState('');
  const [searchTerm, setSearchTerm] = useState('');

  // Charger la liste des utilisateurs au montage
  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await userService.getAllUsers();
      setUsers(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erreur lors du chargement des utilisateurs');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateUser = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.username.trim() || !formData.password.trim()) {
      setError("Le nom d'utilisateur et le mot de passe sont requis");
      return;
    }

    try {
      setError(null);
      const request: CreateUserRequest = {
        username: formData.username,
        password: formData.password,
        role: formData.role,
      };

      const newUser = await userService.createUser(request);
      setUsers([...users, newUser]);
      setFormData({ username: '', password: '', role: 'USER' });
      setShowCreateForm(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erreur lors de la création de l'utilisateur");
    }
  };

  const handleUpdateUser = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!editingUser) return;

    try {
      setError(null);
      const request: UpdateUserRequest = {
        role: editingUser.role,
      };

      if (updatePassword.trim()) {
        request.password = updatePassword;
      }

      const updatedUser = await userService.updateUser(editingUser.username, request);
      setUsers(users.map((u) => (u.username === updatedUser.username ? updatedUser : u)));
      setEditingUser(null);
      setUpdatePassword('');
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Erreur lors de la mise à jour de l'utilisateur"
      );
    }
  };

  const handleDeleteUser = async (username: string) => {
    if (window.confirm(`Êtes-vous sûr de vouloir supprimer l'utilisateur "${username}" ?`)) {
      try {
        setError(null);
        await userService.deleteUser(username);
        setUsers(users.filter((u) => u.username !== username));
      } catch (err) {
        setError(
          err instanceof Error ? err.message : "Erreur lors de la suppression de l'utilisateur"
        );
      }
    }
  };

  const filteredUsers = users.filter((user) =>
    user.username.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (loading) {
    return <div className="admin-panel loading">Chargement des utilisateurs...</div>;
  }

  return (
    <div className="admin-panel">
      <div className="admin-header">
        <h2>Gestion des Utilisateurs</h2>
        <button className="btn btn-primary" onClick={() => setShowCreateForm(!showCreateForm)}>
          {showCreateForm ? '❌ Annuler' : '➕ Nouvel Utilisateur'}
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      {showCreateForm && (
        <form className="form-section" onSubmit={handleCreateUser}>
          <h3>Créer un nouvel utilisateur</h3>
          <div className="form-group">
            <label htmlFor="username">Nom d'utilisateur</label>
            <input
              id="username"
              type="text"
              placeholder="john.doe"
              value={formData.username}
              onChange={(e) => setFormData({ ...formData, username: e.target.value })}
              required
            />
          </div>
          <div className="form-group">
            <label htmlFor="password">Mot de passe</label>
            <input
              id="password"
              type="password"
              placeholder="••••••••"
              value={formData.password}
              onChange={(e) => setFormData({ ...formData, password: e.target.value })}
              required
            />
          </div>
          <div className="form-group">
            <label htmlFor="role">Rôle</label>
            <select
              id="role"
              value={formData.role}
              onChange={(e) => setFormData({ ...formData, role: e.target.value })}
            >
              <option value="USER">USER</option>
              <option value="ADMIN">ADMIN</option>
            </select>
          </div>
          <button type="submit" className="btn btn-success">
            Créer
          </button>
        </form>
      )}

      {editingUser && (
        <form className="form-section" onSubmit={handleUpdateUser}>
          <h3>Modifier l'utilisateur: {editingUser.username}</h3>
          <div className="form-group">
            <label htmlFor="editRole">Rôle</label>
            <select
              id="editRole"
              value={editingUser.role}
              onChange={(e) => setEditingUser({ ...editingUser, role: e.target.value })}
            >
              <option value="USER">USER</option>
              <option value="ADMIN">ADMIN</option>
            </select>
          </div>
          <div className="form-group">
            <label htmlFor="editPassword">Nouveau mot de passe (optionnel)</label>
            <input
              id="editPassword"
              type="password"
              placeholder="Laisser vide pour ne pas changer"
              value={updatePassword}
              onChange={(e) => setUpdatePassword(e.target.value)}
            />
          </div>
          <div className="button-group">
            <button type="submit" className="btn btn-success">
              Sauvegarder
            </button>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => {
                setEditingUser(null);
                setUpdatePassword('');
              }}
            >
              Annuler
            </button>
          </div>
        </form>
      )}

      <div className="users-section">
        <h3>Utilisateurs ({filteredUsers.length})</h3>
        <div className="search-box">
          <input
            type="text"
            placeholder="Rechercher un utilisateur..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-input"
          />
        </div>

        {filteredUsers.length === 0 ? (
          <p className="no-users">Aucun utilisateur trouvé</p>
        ) : (
          <table className="users-table">
            <thead>
              <tr>
                <th>Nom d'utilisateur</th>
                <th>Rôle</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.map((user) => (
                <tr key={user.username}>
                  <td>{user.username}</td>
                  <td>
                    <span className={`role-badge role-${user.role.toLowerCase()}`}>
                      {user.role}
                    </span>
                  </td>
                  <td className="actions">
                    <button
                      className="btn btn-edit"
                      onClick={() => {
                        setEditingUser({ username: user.username, role: user.role });
                        setUpdatePassword('');
                      }}
                      title="Modifier l'utilisateur"
                    >
                      ✏️ Modifier
                    </button>
                    <button
                      className="btn btn-delete"
                      onClick={() => handleDeleteUser(user.username)}
                      title="Supprimer l'utilisateur"
                    >
                      🗑️ Supprimer
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
