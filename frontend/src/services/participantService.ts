import { Participant } from '@/types/participant';
import { apiClient } from './api';

export interface ParticipantsResponse {
  participants: Participant[];
  inPersonTotal: number;
}

export const participantService = {
  /**
   * Récupère les participants d'un meeting
   */
  async getParticipants(meetingId: number): Promise<ParticipantsResponse> {
    const response = await apiClient.get(`/meetings/${meetingId}/participants`);
    return response.data;
  },

  /**
   * Force la re-synchronisation des participants depuis Zoom
   */
  async refreshParticipants(meetingId: number): Promise<ParticipantsResponse> {
    const response = await apiClient.post(`/meetings/${meetingId}/participants/refresh`);
    return response.data;
  },

  /**
   * Sauvegarde les valeurs d'assistance pour un meeting
   * Les valeurs sont organisées par participantId
   */
  async saveAssistance(
    meetingId: number,
    total: number,
    inPersonTotal: number,
    values: Record<number, number>
  ): Promise<void> {
    await apiClient.post(`/meetings/${meetingId}/assistance`, { total, inPersonTotal, values });
  },
};
