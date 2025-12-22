import { AssistanceStatisticsResponse } from '@/types/statistics';
import { apiClient } from './api';

/**
 * Service pour la gestion des statistiques d'assistance
 */
export const statisticsService = {
  /**
   * Récupère les statistiques d'assistance pour une période donnée
   * @param startDate Date de début au format ISO
   * @param endDate Date de fin au format ISO
   * @returns Statistiques d'assistance par jour
   */
  getAssistanceStatistics: async (
    startDate: string,
    endDate: string
  ): Promise<AssistanceStatisticsResponse> => {
    const response = await apiClient.get<AssistanceStatisticsResponse>('/meetings/statistics', {
      params: {
        startDate,
        endDate,
      },
    });
    return response.data;
  },
};
