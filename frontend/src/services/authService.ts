import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  zoomUserId: string;
  zoomAccountId: string;
}

export const authService = {
  /**
   * Connexion utilisateur
   */
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await axios.post<AuthResponse>(`${API_BASE_URL}/auth/login`, credentials);
    return response.data;
  },

  /**
   * Valide le token JWT
   */
  validateToken: async (token: string): Promise<boolean> => {
    try {
      const response = await axios.post<boolean>(
        `${API_BASE_URL}/auth/validate`,
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
