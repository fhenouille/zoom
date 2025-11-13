import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Intercepteur de requête
apiClient.interceptors.request.use(
  (config) => {
    // Ajouter un token d'authentification si nécessaire
    // const token = localStorage.getItem('token');
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`;
    // }
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
      // Rediriger vers la page de connexion
      console.error('Non autorisé');
    }
    return Promise.reject(error);
  }
);
