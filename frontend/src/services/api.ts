import axios from 'axios';

// Production: Use Railway backend, Development: Use localhost
const API_BASE_URL = import.meta.env.DEV 
  ? 'http://localhost:8080/api'
  : 'https://zoom-production-1b9e.up.railway.app/api';

console.log('🔌 API Client - Mode:', import.meta.env.DEV ? 'DEV' : 'PROD', '- URL:', API_BASE_URL);

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Intercepteur de requête
apiClient.interceptors.request.use(
  (config) => {
    // Ajouter le token JWT à chaque requête
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Intercepteur de réponse
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // Gérer les erreurs globalement
    if (error.response?.status === 401) {
      // Rediriger vers la page de connexion si non autorisé
      localStorage.removeItem('authToken');
      localStorage.removeItem('authUser');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
