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
}
