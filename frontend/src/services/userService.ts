import { apiClient } from './api';

export interface CreateUserRequest {
  username: string;
  password: string;
  role: string;
}

export interface UpdateUserRequest {
  role: string;
  password?: string;
}

export interface UserResponse {
  username: string;
  role: string;
}

/**
 * Service pour gérer les utilisateurs (admin only)
 */
export const userService = {
  /**
   * Crée un nouvel utilisateur
   */
  createUser: async (request: CreateUserRequest): Promise<UserResponse> => {
    const { data } = await apiClient.post<UserResponse>('/admin/users', request);
    return data;
  },

  /**
   * Récupère tous les utilisateurs
   */
  getAllUsers: async (): Promise<UserResponse[]> => {
    const { data } = await apiClient.get<UserResponse[]>('/admin/users');
    return data;
  },

  /**
   * Récupère un utilisateur spécifique
   */
  getUser: async (username: string): Promise<UserResponse> => {
    const { data } = await apiClient.get<UserResponse>(`/admin/users/${username}`);
    return data;
  },

  /**
   * Met à jour un utilisateur
   */
  updateUser: async (username: string, request: UpdateUserRequest): Promise<UserResponse> => {
    const { data } = await apiClient.put<UserResponse>(`/admin/users/${username}`, request);
    return data;
  },

  /**
   * Supprime un utilisateur
   */
  deleteUser: async (username: string): Promise<string> => {
    const { data } = await apiClient.delete<string>(`/admin/users/${username}`);
    return data;
  },
};
