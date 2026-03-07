export interface Meeting {
  id: number;
  start: string;
  end: string;
  topic?: string;
  hostName?: string;
  hostEmail?: string;
  duration?: number;
  timezone?: string;
  // Données d'assistance sauvegardées (null si pas encore sauvegardées)
  inPersonTotal?: number | null;
  videoconferenceTotal?: number | null;
  // Indique si la réunion provient des archives (données historiques purgées)
  archived?: boolean;
}

export interface ArchivedMeeting {
  id: number;
  meetingId: number;
  startTime: string;
  endTime: string;
  timezone?: string;
  inPersonTotal: number;
  remoteTotal: number;
  archivedAt: string;
}
