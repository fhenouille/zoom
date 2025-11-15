export interface Participant {
  id: number;
  userId: string;
  name: string;
  durationMinutes: number;
  joinTime?: string;
  leaveTime?: string;
  assistanceValue?: number | null;
}
