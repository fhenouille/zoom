package com.zoom.controller;

import com.zoom.entity.Meeting;
import com.zoom.service.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * Récupère toutes les réunions
     */
    @GetMapping
    public ResponseEntity<List<Meeting>> getAllMeetings() {
        log.info("GET /api/meetings - Récupération de toutes les réunions");
        List<Meeting> meetings = meetingService.getAllMeetings();
        return ResponseEntity.ok(meetings);
    }

    /**
     * Récupère une réunion par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Meeting> getMeetingById(@PathVariable Long id) {
        log.info("GET /api/meetings/{} - Récupération de la réunion", id);
        return meetingService.getMeetingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crée une nouvelle réunion
     */
    @PostMapping
    public ResponseEntity<Meeting> createMeeting(@Valid @RequestBody Meeting meeting) {
        log.info("POST /api/meetings - Création d'une nouvelle réunion");
        Meeting createdMeeting = meetingService.createMeeting(meeting);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMeeting);
    }

    /**
     * Met à jour une réunion existante
     */
    @PutMapping("/{id}")
    public ResponseEntity<Meeting> updateMeeting(
            @PathVariable Long id,
            @Valid @RequestBody Meeting meeting) {
        log.info("PUT /api/meetings/{} - Mise à jour de la réunion", id);
        try {
            Meeting updatedMeeting = meetingService.updateMeeting(id, meeting);
            return ResponseEntity.ok(updatedMeeting);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Supprime une réunion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeeting(@PathVariable Long id) {
        log.info("DELETE /api/meetings/{} - Suppression de la réunion", id);
        meetingService.deleteMeeting(id);
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
}
