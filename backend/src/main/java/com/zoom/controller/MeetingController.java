package com.zoom.controller;

import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.zoom.dto.*;
import com.zoom.entity.Meeting;
import com.zoom.entity.MeetingAssistance;
import com.zoom.repository.MeetingAssistanceRepository;
import com.zoom.service.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Contrôleur REST pour la gestion des réunions
 */
@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class MeetingController {

    private final MeetingService meetingService;
    private final ParticipantService participantService;
    private final ZoomApiService zoomApiService;
    private final MeetingAssistanceRepository meetingAssistanceRepository;

    /**
     * Récupère toutes les réunions avec filtres optionnels de date et données d'assistance
     */
    @GetMapping
    public ResponseEntity<List<MeetingWithAssistance>> getAllMeetings(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endDate) {
        log.info("📥 GET /api/meetings - Récupération des réunions (startDate: {}, endDate: {})", startDate, endDate);
        long startTime = System.currentTimeMillis();

        List<MeetingWithAssistance> meetings = meetingService.getMeetingsByDateRangeWithAssistance(startDate, endDate);

        long duration = System.currentTimeMillis() - startTime;
        log.info("📤 GET /api/meetings - Réponse: {} meetings en {}ms", meetings.size(), duration);

        return ResponseEntity.ok(meetings);
    }

    /**
     * Récupère une réunion par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Meeting> getMeetingById(@PathVariable Long id) {
        log.info("📥 GET /api/meetings/{} - Récupération de la réunion", id);
        ResponseEntity<Meeting> response = meetingService.getMeetingById(id)
                .map(meeting -> {
                    log.info("📤 GET /api/meetings/{} - Trouvé: '{}'", id, meeting.getTopic());
                    return ResponseEntity.ok(meeting);
                })
                .orElseGet(() -> {
                    log.warn("⚠️ GET /api/meetings/{} - Non trouvé", id);
                    return ResponseEntity.notFound().build();
                });
        return response;
    }

    /**
     * Crée une nouvelle réunion
     */
    @PostMapping
    public ResponseEntity<Meeting> createMeeting(@Valid @RequestBody Meeting meeting) {
        log.info("📥 POST /api/meetings - Création d'une nouvelle réunion: '{}'", meeting.getTopic());
        log.debug("Données reçues: start={}, end={}", meeting.getStart(), meeting.getEnd());

        Meeting createdMeeting = meetingService.createMeeting(meeting);

        log.info("📤 POST /api/meetings - Créé avec ID={}", createdMeeting.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMeeting);
    }

    /**
     * Met à jour une réunion existante
     */
    @PutMapping("/{id}")
    public ResponseEntity<Meeting> updateMeeting(
            @PathVariable Long id,
            @Valid @RequestBody Meeting meeting) {
        log.info("📥 PUT /api/meetings/{} - Mise à jour de la réunion", id);
        log.debug("Nouvelles données: topic='{}', start={}, end={}", meeting.getTopic(), meeting.getStart(), meeting.getEnd());

        try {
            Meeting updatedMeeting = meetingService.updateMeeting(id, meeting);
            log.info("📤 PUT /api/meetings/{} - Mise à jour réussie", id);
            return ResponseEntity.ok(updatedMeeting);
        } catch (RuntimeException e) {
            log.warn("⚠️ PUT /api/meetings/{} - Non trouvé: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Supprime une réunion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeeting(@PathVariable Long id) {
        log.info("📥 DELETE /api/meetings/{} - Suppression de la réunion", id);
        meetingService.deleteMeeting(id);
        log.info("📤 DELETE /api/meetings/{} - Supprimé avec succès", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupère les réunions à venir
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<Meeting>> getUpcomingMeetings() {
        log.info("GET /api/meetings/upcoming - Récupération des réunions à venir");
        List<Meeting> meetings = meetingService.getUpcomingMeetings();
        return ResponseEntity.ok(meetings);
    }

    /**
     * Récupère les participants d'un meeting avec les valeurs d'assistance
     * Si non présents en base, les récupère depuis l'API Zoom
     */
    @GetMapping("/{id}/participants")
    public ResponseEntity<ParticipantsResponse> getMeetingParticipants(@PathVariable Long id) {
        log.info("📥 GET /api/meetings/{}/participants - Récupération des participants", id);
        long startTime = System.currentTimeMillis();

        ParticipantsResponse response = participantService.getParticipants(id);

        long duration = System.currentTimeMillis() - startTime;
        log.info("📤 GET /api/meetings/{}/participants - Réponse: {} participants en {}ms",
            id, response.getParticipants().size(), duration);

        return ResponseEntity.ok(response);
    }

    /**
     * Force la re-synchronisation des participants depuis Zoom
     */
    @PostMapping("/{id}/participants/refresh")
    public ResponseEntity<ParticipantsResponse> refreshParticipants(@PathVariable Long id) {
        log.info("📥 POST /api/meetings/{}/participants/refresh - Re-synchronisation depuis Zoom", id);
        long startTime = System.currentTimeMillis();

        ParticipantsResponse response = participantService.refreshParticipants(id);

        long duration = System.currentTimeMillis() - startTime;
        log.info("📤 POST /api/meetings/{}/participants/refresh - Réponse: {} participants en {}ms",
            id, response.getParticipants().size(), duration);

        return ResponseEntity.ok(response);
    }

    /**
     * Récupère les résultats des sondages d'un meeting depuis l'API Zoom
     */
    @GetMapping("/{id}/polls")
    public ResponseEntity<ZoomPollResponse> getMeetingPolls(@PathVariable Long id) {
        log.info("📥 GET /api/meetings/{}/polls - Récupération des résultats de sondage", id);
        long startTime = System.currentTimeMillis();

        // Récupère le meeting pour obtenir le UUID
        Meeting meeting = meetingService.getMeetingById(id)
                .orElseThrow(() -> new RuntimeException("Meeting non trouvé avec l'ID: " + id));

        // Récupère les résultats de sondage depuis Zoom
        ZoomPollResponse pollResponse = zoomApiService.getPollResults(meeting.getZoomUuid());

        long duration = System.currentTimeMillis() - startTime;

        if (pollResponse == null || pollResponse.getParticipants() == null || pollResponse.getParticipants().isEmpty()) {
            log.info("📤 GET /api/meetings/{}/polls - Aucun sondage trouvé en {}ms", id, duration);
            return ResponseEntity.noContent().build();
        }

        log.info("📤 GET /api/meetings/{}/polls - Réponse: {} réponses au sondage en {}ms",
            id, pollResponse.getParticipants().size(), duration);

        return ResponseEntity.ok(pollResponse);
    }

    /**
     * Sauvegarde les valeurs d'assistance pour un meeting
     */
    @PostMapping("/{id}/assistance")
    public ResponseEntity<Void> saveAssistance(@PathVariable Long id, @RequestBody @Valid AssistanceSaveRequest request) {
        log.info("💾 POST /api/meetings/{}/assistance - Sauvegarde de l'assistance", id);
        long startTime = System.currentTimeMillis();

        // Vérifie que le meeting existe
        Meeting meeting = meetingService.getMeetingById(id)
                .orElseThrow(() -> new RuntimeException("Meeting non trouvé avec l'ID: " + id));

        // Supprime l'ancienne assistance si elle existe
        meetingAssistanceRepository.findByMeetingId(id).ifPresent(existing -> {
            meetingAssistanceRepository.delete(existing);
            log.info("🗑️ Ancienne assistance supprimée pour le meeting {}", id);
        });

        // Sauvegarde la nouvelle assistance
        MeetingAssistance assistance = new MeetingAssistance(meeting, request.getTotal(), request.getInPersonTotal(), request.getValues());
        meetingAssistanceRepository.save(assistance);

        long duration = System.currentTimeMillis() - startTime;
        log.info("✓ POST /api/meetings/{}/assistance - Assistance sauvegardée (total: {}) en {}ms",
            id, request.getTotal(), duration);

        return ResponseEntity.ok().build();
    }

    /**
     * Récupère les statistiques d'assistance pour une période donnée
     */
    @GetMapping("/statistics")
    public ResponseEntity<AssistanceStatisticsResponse> getAssistanceStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endDate) {
        log.info("📊 GET /api/meetings/statistics - Récupération des statistiques (startDate: {}, endDate: {})", startDate, endDate);
        long startTime = System.currentTimeMillis();

        // Si les dates ne sont pas fournies, utilise les 30 derniers jours
        if (startDate == null) {
            startDate = java.time.LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = java.time.LocalDateTime.now();
        }

        AssistanceStatisticsResponse statistics = meetingService.getAssistanceStatistics(startDate, endDate);

        long duration = System.currentTimeMillis() - startTime;
        log.info("📤 GET /api/meetings/statistics - Réponse: {} jours avec données en {}ms",
            statistics.getDailyStats().size(), duration);

        return ResponseEntity.ok(statistics);
    }
}
