package com.zoom.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.zoom.entity.Meeting;
import com.zoom.entity.Participant;
import com.zoom.service.MeetingService;
import com.zoom.service.ParticipantService;

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

    /**
     * Récupère toutes les réunions
     */
    @GetMapping
    public ResponseEntity<List<Meeting>> getAllMeetings() {
        log.info("📥 GET /api/meetings - Récupération de toutes les réunions");
        long startTime = System.currentTimeMillis();

        List<Meeting> meetings = meetingService.getAllMeetings();

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
     * Récupère les participants d'un meeting
     * Si non présents en base, les récupère depuis l'API Zoom
     */
    @GetMapping("/{id}/participants")
    public ResponseEntity<List<Participant>> getMeetingParticipants(@PathVariable Long id) {
        log.info("📥 GET /api/meetings/{}/participants - Récupération des participants", id);
        long startTime = System.currentTimeMillis();

        List<Participant> participants = participantService.getParticipants(id);

        long duration = System.currentTimeMillis() - startTime;
        log.info("📤 GET /api/meetings/{}/participants - Réponse: {} participants en {}ms",
            id, participants.size(), duration);

        return ResponseEntity.ok(participants);
    }

    /**
     * Force la re-synchronisation des participants depuis Zoom
     */
    @PostMapping("/{id}/participants/refresh")
    public ResponseEntity<List<Participant>> refreshMeetingParticipants(@PathVariable Long id) {
        log.info("📥 POST /api/meetings/{}/participants/refresh - Re-synchronisation forcée", id);
        long startTime = System.currentTimeMillis();

        List<Participant> participants = participantService.refreshParticipants(id);

        long duration = System.currentTimeMillis() - startTime;
        log.info("📤 POST /api/meetings/{}/participants/refresh - Réponse: {} participants en {}ms",
            id, participants.size(), duration);

        return ResponseEntity.ok(participants);
    }
}
