import { apiClient } from './api';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  role: string;
}

export const authService = {
  /**
   * Connexion utilisateur
   */
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>(`/auth/login`, credentials);
    return response.data;
  },

  /**
   * Valide le token JWT
   */
  validateToken: async (token: string): Promise<boolean> => {
    try {
      const response = await apiClient.post<boolean>(
        `/auth/validate`,
        {},
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      return response.data;
    } catch {
      return false;
    }
  },
};
