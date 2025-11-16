import axios from 'axios';

// Get API URL from Vite define or fallback
declare const __API_BASE_URL__: string;
const API_BASE_URL = typeof __API_BASE_URL__ !== 'undefined' 
  ? __API_BASE_URL__ 
  : (import.meta.env.VITE_API_BASE_URL as string | undefined) 
  || 'http://localhost:8080/api';

console.log('🔌 API Client initialized with:', API_BASE_URL);

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
