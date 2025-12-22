export interface DailyAssistanceStats {
  date: string; // Format ISO (YYYY-MM-DD)
  inPerson: number; // Nombre de personnes en présentiel
  remote: number; // Nombre de personnes en visio
  total: number; // Total d'assistance
  meetingCount: number; // Nombre de réunions ce jour-là
}

export interface AssistanceStatisticsResponse {
  dailyStats: DailyAssistanceStats[];
  startDate: string;
  endDate: string;
}
