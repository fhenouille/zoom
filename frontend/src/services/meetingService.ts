import { Meeting } from '@/types/meeting';
import { apiClient } from './api';

export const meetingService = {
  /**
   * Récupère toutes les réunions
   */
  getAllMeetings: async (): Promise<Meeting[]> => {
    const response = await apiClient.get<Meeting[]>('/meetings');
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
};
