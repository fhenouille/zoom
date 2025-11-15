import { Meeting } from '@/types/meeting';
import { apiClient } from './api';

// Interface pour les résultats de sondages
export interface PollAnswer {
  question: string;
  answer: string;
  polling_id: string;
  date_time: string;
}

export interface PollResult {
  email: string | null;
  name: string;
  first_name: string;
  question_details: PollAnswer[];
}

export interface PollResponse {
  id: number;
  uuid: string;
  start_time: string;
  participants: PollResult[];
}

export const meetingService = {
  /**
   * Récupère toutes les réunions avec filtres optionnels de date
   */
  getAllMeetings: async (startDate?: string, endDate?: string): Promise<Meeting[]> => {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);

    const response = await apiClient.get<Meeting[]>(
      `/meetings${params.toString() ? `?${params.toString()}` : ''}`
    );
    return response.data;
  },

  /**
   * Récupère une réunion par son ID
   */
  getMeetingById: async (id: number): Promise<Meeting> => {
    const response = await apiClient.get<Meeting>(`/meetings/${id}`);
    return response.data;
  },

  /**
   * Crée une nouvelle réunion
   */
  createMeeting: async (meeting: Omit<Meeting, 'id'>): Promise<Meeting> => {
    const response = await apiClient.post<Meeting>('/meetings', meeting);
    return response.data;
  },

  /**
   * Récupère les résultats de sondages d'une réunion
   */
  getMeetingPolls: async (id: number): Promise<PollResponse | null> => {
    try {
      const response = await apiClient.get<PollResponse>(`/meetings/${id}/polls`);
      return response.data;
    } catch (error: any) {
      // Si 204 No Content, il n'y a pas de sondage
      if (error.response?.status === 204) {
        return null;
      }
      throw error;
    }
  },
};
