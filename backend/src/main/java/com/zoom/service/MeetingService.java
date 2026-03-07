package com.zoom.service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zoom.dto.*;
import com.zoom.dto.AssistanceStatisticsResponse.DailyAssistanceStats;
import com.zoom.entity.*;
import com.zoom.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service pour la gestion des réunions
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final ZoomApiService zoomApiService;
    private final MeetingAssistanceRepository meetingAssistanceRepository;
    private final MeetingArchiveRepository meetingArchiveRepository;

    /**
     * Récupère toutes les réunions
     * Interroge d'abord l'API Zoom pour synchroniser les meetings passés des 5 derniers jours
     */
    @Transactional
    public List<Meeting> getAllMeetings() {
        log.info("Récupération de toutes les réunions");

        // Synchronise avec Zoom avant de retourner les données
        syncMeetingsFromZoom();

        return meetingRepository.findAll();
    }

    /**
     * Récupère les réunions filtrées par date avec les données d'assistance
     * Synchronise toujours avec Zoom d'abord, puis applique les filtres si fournis
     */
    @Transactional
    public List<MeetingWithAssistance> getMeetingsByDateRangeWithAssistance(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Récupération des réunions avec assistance (startDate: {}, endDate: {})", startDate, endDate);

        // Récupère les meetings
        List<Meeting> meetings = getMeetingsByDateRange(startDate, endDate);

        // Convertit en DTO avec les données d'assistance
        return meetings.stream()
                .map(this::convertToMeetingWithAssistance)
                .collect(Collectors.toList());
    }

    /**
     * Convertit une entité Meeting en DTO MeetingWithAssistance
     */
    private MeetingWithAssistance convertToMeetingWithAssistance(Meeting meeting) {
        MeetingWithAssistance dto = new MeetingWithAssistance();
        dto.setId(meeting.getId());
        dto.setStart(meeting.getStart());
        dto.setEnd(meeting.getEnd());
        dto.setTopic(meeting.getTopic());
        dto.setHostName(meeting.getHostName());
        dto.setHostEmail(meeting.getHostEmail());
        dto.setDuration(meeting.getDuration());
        dto.setTimezone(meeting.getTimezone());

        // Récupère les données d'assistance si elles existent
        Optional<MeetingAssistance> assistanceOpt = meetingAssistanceRepository.findByMeetingId(meeting.getId());
        if (assistanceOpt.isPresent()) {
            MeetingAssistance assistance = assistanceOpt.get();
            dto.setInPersonTotal(assistance.getInPersonTotal());
            dto.setVideoconferenceTotal(assistance.getTotal());
        } else {
            dto.setInPersonTotal(null);
            dto.setVideoconferenceTotal(null);
        }

        return dto;
    }

    /**
     * Récupère les réunions filtrées par date
     * Synchronise toujours avec Zoom d'abord, puis applique les filtres si fournis
     */
    @Transactional
    public List<Meeting> getMeetingsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Récupération des réunions (startDate: {}, endDate: {})", startDate, endDate);

        // Synchronise avec Zoom en utilisant les dates du filtre (ou par défaut 5 derniers jours)
        if (startDate != null && endDate != null) {
            syncMeetingsFromZoom(startDate.toLocalDate(), endDate.toLocalDate());
        } else {
            syncMeetingsFromZoom();
        }

        // Si aucun filtre n'est fourni, retourne tout
        if (startDate == null && endDate == null) {
            return meetingRepository.findAll();
        }

        // Filtre par date
        if (startDate != null && endDate != null) {
            return meetingRepository.findByStartBetween(startDate, endDate);
        } else if (startDate != null) {
            return meetingRepository.findByStartAfter(startDate);
        } else {
            return meetingRepository.findByStartBefore(endDate);
        }
    }

    /**
     * Synchronise les meetings depuis l'API Zoom (par défaut 5 derniers jours)
     */
    public void syncMeetingsFromZoom() {
        LocalDate today = LocalDate.now();
        LocalDate fiveDaysAgo = today.minusDays(5);
        syncMeetingsFromZoom(fiveDaysAgo, today);
    }

    /**
     * Synchronise les meetings depuis l'API Zoom pour une période donnée
     */
    public void syncMeetingsFromZoom(LocalDate fromDate, LocalDate toDate) {
        try {
            log.info("🔄 Synchronisation des meetings depuis Zoom ({} à {})", fromDate, toDate);
            long startTime = System.currentTimeMillis();

            // Récupère les meetings depuis Zoom pour la période demandée
            log.debug("⏳ Appel de l'API Zoom...");
            List<ZoomMeeting> zoomMeetings = zoomApiService.getPastMeetings(fromDate, toDate);
            log.info("📥 {} meetings reçus de Zoom", zoomMeetings.size());

            int newMeetingsCount = 0;
            int existingMeetingsCount = 0;
            int errorCount = 0;

            for (int i = 0; i < zoomMeetings.size(); i++) {
                ZoomMeeting zoomMeeting = zoomMeetings.get(i);
                String zoomMeetingId = String.valueOf(zoomMeeting.getId());
                String zoomUuid = zoomMeeting.getUuid();

                log.debug("[{}/{}] Traitement session UUID={}, meeting ID={}, topic='{}'",
                    i + 1, zoomMeetings.size(), zoomUuid, zoomMeetingId, zoomMeeting.getTopic());

                try {
                    // Vérifie si la session existe déjà en base (par UUID unique)
                    if (!meetingRepository.existsByZoomUuid(zoomUuid)) {
                        // Convertit le ZoomMeeting en Meeting entity
                        Meeting meeting = convertZoomMeetingToEntity(zoomMeeting);
                        Meeting savedMeeting = meetingRepository.save(meeting);
                        newMeetingsCount++;
                        log.info("  ✓ Nouvelle session ajoutée: id={}, uuid={}, topic='{}', start={}",
                            savedMeeting.getId(), zoomUuid, savedMeeting.getTopic(), savedMeeting.getStart());
                    } else {
                        existingMeetingsCount++;
                        log.debug("  ○ Session déjà existante: {}", zoomUuid);
                    }
                } catch (Exception e) {
                    errorCount++;
                    log.error("  ❌ Erreur lors du traitement de la session {}: {}", zoomUuid, e.getMessage());
                    log.debug("Stack trace:", e);
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ Synchronisation terminée en {}ms: {} nouvelles sessions, {} existantes, {} erreurs",
                    duration, newMeetingsCount, existingMeetingsCount, errorCount);

        } catch (Exception e) {
            log.error("❌ Erreur lors de la synchronisation avec Zoom: {}", e.getMessage());
            log.debug("Stack trace complète:", e);
            // On ne propage pas l'erreur pour permettre de continuer avec les données en base
        }
    }

    /**
     * Convertit un ZoomMeeting (DTO) en Meeting (entité)
     */
    private Meeting convertZoomMeetingToEntity(ZoomMeeting zoomMeeting) {
        Meeting meeting = new Meeting();

        // ID Zoom
        meeting.setZoomMeetingId(String.valueOf(zoomMeeting.getId()));
        meeting.setZoomUuid(zoomMeeting.getUuid());

        // Informations du meeting
        meeting.setTopic(zoomMeeting.getTopic());
        meeting.setType(zoomMeeting.getType());
        meeting.setDuration(zoomMeeting.getDuration());
        meeting.setTimezone(zoomMeeting.getTimezone());

        // Informations de l'hôte
        meeting.setHostName(zoomMeeting.getUserName());
        meeting.setHostEmail(zoomMeeting.getHostEmail());

        // Dates (parse ISO 8601 format from Zoom et convertit en heure française)
        if (zoomMeeting.getStartTime() != null) {
            try {
                // Parse la date UTC de Zoom
                ZonedDateTime startZoned = ZonedDateTime.parse(zoomMeeting.getStartTime(),
                        DateTimeFormatter.ISO_DATE_TIME);
                // Convertit en fuseau horaire français (Europe/Paris = UTC+1/UTC+2)
                ZonedDateTime startParis = startZoned.withZoneSameInstant(ZoneId.of("Europe/Paris"));
                meeting.setStart(startParis.toLocalDateTime());

                // Utilise end_time si disponible (depuis l'API Report)
                if (zoomMeeting.getEndTime() != null) {
                    ZonedDateTime endZoned = ZonedDateTime.parse(zoomMeeting.getEndTime(),
                            DateTimeFormatter.ISO_DATE_TIME);
                    ZonedDateTime endParis = endZoned.withZoneSameInstant(ZoneId.of("Europe/Paris"));
                    meeting.setEnd(endParis.toLocalDateTime());
                } else if (zoomMeeting.getDuration() != null) {
                    // Sinon calcule la date de fin en ajoutant la durée
                    meeting.setEnd(meeting.getStart().plusMinutes(zoomMeeting.getDuration()));
                } else {
                    // Durée par défaut de 60 minutes si non spécifiée
                    meeting.setEnd(meeting.getStart().plusMinutes(60));
                }
            } catch (Exception e) {
                log.warn("Impossible de parser la date du meeting: {}", zoomMeeting.getStartTime(), e);
                // Utilise des valeurs par défaut
                meeting.setStart(LocalDateTime.now().minusDays(1));
                meeting.setEnd(LocalDateTime.now());
            }
        } else {
            // Valeurs par défaut si pas de date
            meeting.setStart(LocalDateTime.now().minusDays(1));
            meeting.setEnd(LocalDateTime.now());
        }

        return meeting;
    }

    /**
     * Récupère une réunion par son ID
     */
    @Transactional(readOnly = true)
    public Optional<Meeting> getMeetingById(Long id) {
        log.info("Récupération de la réunion avec l'ID: {}", id);
        return meetingRepository.findById(id);
    }

    /**
     * Crée une nouvelle réunion
     */
    public Meeting createMeeting(Meeting meeting) {
        log.info("Création d'une nouvelle réunion: {}", meeting);
        validateMeeting(meeting);
        return meetingRepository.save(meeting);
    }

    /**
     * Met à jour une réunion existante
     */
    public Meeting updateMeeting(Long id, Meeting meeting) {
        log.info("Mise à jour de la réunion avec l'ID: {}", id);
        Meeting existingMeeting = meetingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réunion non trouvée avec l'ID: " + id));

        existingMeeting.setStart(meeting.getStart());
        existingMeeting.setEnd(meeting.getEnd());
        validateMeeting(existingMeeting);

        return meetingRepository.save(existingMeeting);
    }

    /**
     * Supprime une réunion
     */
    public void deleteMeeting(Long id) {
        log.info("Suppression de la réunion avec l'ID: {}", id);
        meetingRepository.deleteById(id);
    }

    /**
     * Valide qu'une réunion est cohérente
     */
    private void validateMeeting(Meeting meeting) {
        if (meeting.getEnd().isBefore(meeting.getStart())) {
            throw new IllegalArgumentException("La date de fin ne peut pas être avant la date de début");
        }
    }

    /**
     * Récupère les réunions à venir
     */
    @Transactional(readOnly = true)
    public List<Meeting> getUpcomingMeetings() {
        log.info("Récupération des réunions à venir");
        return meetingRepository.findByStartAfter(LocalDateTime.now());
    }

    /**
     * Récupère les statistiques d'assistance pour une période donnée
     * Regroupe les données par jour, en fusionnant les réunions actives et archivées
     */
    @Transactional(readOnly = true)
    public AssistanceStatisticsResponse getAssistanceStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Récupération des statistiques d'assistance (startDate: {}, endDate: {})", startDate, endDate);

        // Map pour regrouper les statistiques par jour
        Map<LocalDate, DailyAssistanceStats> dailyStatsMap = new HashMap<>();

        // --- Réunions actives (non purgées) ---
        List<Meeting> meetings = meetingRepository.findByStartBetween(startDate, endDate);
        for (Meeting meeting : meetings) {
            Optional<MeetingAssistance> assistanceOpt = meetingAssistanceRepository.findByMeetingId(meeting.getId());
            if (assistanceOpt.isPresent()) {
                MeetingAssistance assistance = assistanceOpt.get();
                LocalDate date = meeting.getStart().toLocalDate();

                DailyAssistanceStats stats = dailyStatsMap.computeIfAbsent(date, d -> {
                    DailyAssistanceStats newStats = new DailyAssistanceStats();
                    newStats.setDate(d.toString());
                    newStats.setInPerson(0);
                    newStats.setRemote(0);
                    newStats.setTotal(0);
                    newStats.setMeetingCount(0);
                    return newStats;
                });

                // Le champ "total" dans MeetingAssistance représente uniquement les participants en visio
                stats.setInPerson(stats.getInPerson() + assistance.getInPersonTotal());
                stats.setRemote(stats.getRemote() + assistance.getTotal());
                stats.setTotal(stats.getTotal() + assistance.getTotal() + assistance.getInPersonTotal());
                stats.setMeetingCount(stats.getMeetingCount() + 1);
            }
        }

        // --- Réunions archivées (purgées) ---
        List<MeetingArchive> archives = meetingArchiveRepository.findByStartTimeBetween(startDate, endDate);
        for (MeetingArchive archive : archives) {
            LocalDate date = archive.getStartTime().toLocalDate();

            DailyAssistanceStats stats = dailyStatsMap.computeIfAbsent(date, d -> {
                DailyAssistanceStats newStats = new DailyAssistanceStats();
                newStats.setDate(d.toString());
                newStats.setInPerson(0);
                newStats.setRemote(0);
                newStats.setTotal(0);
                newStats.setMeetingCount(0);
                return newStats;
            });

            stats.setInPerson(stats.getInPerson() + archive.getInPersonTotal());
            stats.setRemote(stats.getRemote() + archive.getRemoteTotal());
            stats.setTotal(stats.getTotal() + archive.getInPersonTotal() + archive.getRemoteTotal());
            stats.setMeetingCount(stats.getMeetingCount() + 1);
        }

        // Convertit la map en liste triée par date
        List<DailyAssistanceStats> dailyStatsList = dailyStatsMap.values().stream()
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());

        AssistanceStatisticsResponse response = new AssistanceStatisticsResponse();
        response.setDailyStats(dailyStatsList);
        response.setStartDate(startDate);
        response.setEndDate(endDate);

        log.info("Statistiques récupérées: {} jours avec données ({} actives, {} archivées)",
                dailyStatsList.size(), meetings.size(), archives.size());
        return response;
    }
}
