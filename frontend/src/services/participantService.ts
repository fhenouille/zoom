import { Participant } from '@/types/participant';
import { apiClient } from './api';

export const participantService = {
  /**
   * Récupère les participants d'un meeting
   */
  async getParticipants(meetingId: number): Promise<Participant[]> {
    const response = await apiClient.get(`/meetings/${meetingId}/participants`);
    return response.data;
  },

  /**
   * Force la re-synchronisation des participants depuis Zoom
   */
  async refreshParticipants(meetingId: number): Promise<Participant[]> {
    const response = await apiClient.post(`/meetings/${meetingId}/participants/refresh`);
    return response.data;
  },
};
