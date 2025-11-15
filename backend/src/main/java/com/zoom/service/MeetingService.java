package com.zoom.service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zoom.dto.ZoomMeeting;
import com.zoom.entity.Meeting;
import com.zoom.repository.MeetingRepository;

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
}
