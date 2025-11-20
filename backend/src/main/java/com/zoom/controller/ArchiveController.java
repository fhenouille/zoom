package com.zoom.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.zoom.entity.MeetingArchive;
import com.zoom.repository.MeetingArchiveRepository;
import com.zoom.service.DataPurgeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Contrôleur REST pour la gestion de l'archivage et la purge des données
 */
@RestController
@RequestMapping("/api/archive")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class ArchiveController {

    private final DataPurgeService dataPurgeService;
    private final MeetingArchiveRepository meetingArchiveRepository;

    /**
     * Récupère toutes les réunions archivées
     */
    @GetMapping("/meetings")
    public ResponseEntity<List<MeetingArchive>> getArchivedMeetings() {
        log.info("Récupération de toutes les réunions archivées");
        List<MeetingArchive> archives = meetingArchiveRepository.findAll();
        return ResponseEntity.ok(archives);
    }

    /**
     * Récupère les réunions archivées dans une plage de dates
     */
    @GetMapping("/meetings/range")
    public ResponseEntity<List<MeetingArchive>> getArchivedMeetingsByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        log.info("Récupération des réunions archivées entre {} et {}", startTime, endTime);
        List<MeetingArchive> archives = meetingArchiveRepository.findByStartTimeBetween(startTime, endTime);
        return ResponseEntity.ok(archives);
    }

    /**
     * Récupère une réunion archivée par son ID
     */
    @GetMapping("/meetings/{id}")
    public ResponseEntity<MeetingArchive> getArchivedMeeting(@PathVariable Long id) {
        log.info("Récupération de la réunion archivée {}", id);
        return meetingArchiveRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Récupère les statistiques d'archivage
     */
    @GetMapping("/stats")
    public ResponseEntity<DataPurgeService.ArchiveStats> getArchiveStats() {
        log.info("Récupération des statistiques d'archivage");
        return ResponseEntity.ok(dataPurgeService.getArchiveStats());
    }

    /**
     * Déclenche manuellement la purge des données
     * Cette API est principalement pour les tests et la maintenance administrative
     */
    @PostMapping("/purge")
    public ResponseEntity<String> triggerManualPurge() {
        try {
            log.warn("Déclenchement manuel de la purge des données");
            dataPurgeService.purgeOldData();
            return ResponseEntity.ok("Purge des données déclenchée avec succès");
        } catch (Exception e) {
            log.error("Erreur lors du déclenchement de la purge manuelle", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur lors de la purge: " + e.getMessage());
        }
    }

    /**
     * Supprime une réunion archivée (par exemple si elle est trop vieille)
     * Cette API est pour la maintenance administrative
     */
    @DeleteMapping("/meetings/{id}")
    public ResponseEntity<String> deleteArchivedMeeting(@PathVariable Long id) {
        try {
            log.warn("Suppression manuelle de la réunion archivée {}", id);
            meetingArchiveRepository.deleteById(id);
            return ResponseEntity.ok("Réunion archivée supprimée avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la réunion archivée {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    /**
     * Supprime les archives de réunion plus anciennes qu'une date donnée
     */
    @DeleteMapping("/before")
    public ResponseEntity<String> deleteArchivesBefore(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cutoffDate) {
        try {
            log.warn("Suppression des archives antérieures à {}", cutoffDate);
            meetingArchiveRepository.deleteArchivedBefore(cutoffDate);
            return ResponseEntity.ok("Archives supprimées avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de la suppression des archives", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur lors de la suppression: " + e.getMessage());
        }
    }
}
