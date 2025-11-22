import axios from 'axios';

// Production: Use Railway backend, Development: Use localhost
// In production, __API_BASE_URL__ is injected by Vite define()
// In development, we use localhost
declare const __API_BASE_URL__: string;
const API_BASE_URL =
  typeof __API_BASE_URL__ !== 'undefined' ? __API_BASE_URL__ : 'http://localhost:8080/api';

console.log('🔌 API Client - URL:', API_BASE_URL);

// Stockage du token en mémoire uniquement (pas de persistence)
let authToken: string | null = null;

export const setAuthToken = (token: string | null) => {
  authToken = token;
};

export const getAuthToken = () => authToken;

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Intercepteur de requête
apiClient.interceptors.request.use(
  (config) => {
    // Ajouter le token JWT à chaque requête (depuis la mémoire uniquement)
    if (authToken) {
      config.headers.Authorization = `Bearer ${authToken}`;
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
      // Vérifier si c'est une erreur de login (pas d'endpoint /auth/login)
      // Rediriger seulement si le token est invalide/expiré (pas une simple erreur de credentials)
      if (!error.config?.url?.includes('/auth/login')) {
        // Token invalide ou expiré : rediriger vers la page de connexion
        authToken = null;
        globalThis.location.href = '/login';
      }
      // Pour /auth/login, laisser le frontend gérer l'erreur (message d'erreur sur la page)
    }
    return Promise.reject(error);
  }
);
