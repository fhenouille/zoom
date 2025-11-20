package com.zoom.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zoom.entity.*;
import com.zoom.repository.*;

import lombok.extern.slf4j.Slf4j;

/**
 * Service pour la gestion de la purge et archivage des données de réunions.
 * Archiváge les données agrégées avant suppression des données détaillées après une période de rétention.
 */
@Service
@Transactional
@EnableScheduling
@Slf4j
public class DataPurgeService {

    private final MeetingRepository meetingRepository;
    private final MeetingAssistanceRepository meetingAssistanceRepository;
    private final ParticipantRepository participantRepository;
    private final MeetingArchiveRepository meetingArchiveRepository;
    
    // Nombre de jours avant purge (par défaut 90)
    @Value("${app.data.retention.days:90}")
    private int retentionDays;

    public DataPurgeService(MeetingRepository meetingRepository,
                           MeetingAssistanceRepository meetingAssistanceRepository,
                           ParticipantRepository participantRepository,
                           MeetingArchiveRepository meetingArchiveRepository,
                           DataPurgeService self) {
        this.meetingRepository = meetingRepository;
        this.meetingAssistanceRepository = meetingAssistanceRepository;
        this.participantRepository = participantRepository;
        this.meetingArchiveRepository = meetingArchiveRepository;
        this.self = self;
    }

    /**
     * Tâche planifiée pour la purge automatique des données anciennes
     * S'exécute tous les jours à 2h du matin UTC
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "UTC")
    @Transactional
    public void schedulePurgeTask() {
        log.info("Démarrage de la tâche de purge des données planifiée");
        try {
            purgeOldData();
            log.info("Tâche de purge des données terminée avec succès");
        } catch (Exception e) {
            log.error("Erreur lors de la tâche de purge des données", e);
        }
    }

    /**
     * Méthode principale pour archiver et purger les données
     * 1. Identifie les réunions dont la date de purge est passée
     * 2. Archive les données agrégées
     * 3. Supprime les données détaillées
     */
    @Transactional
    public void purgeOldData() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        log.info("Recherche des réunions à archiver/purger avant le {}", cutoffDate);

        // Trouve toutes les réunions terminées il y a plus de 90 jours
        List<Meeting> oldMeetings = meetingRepository.findAll().stream()
            .filter(m -> m.getEnd() != null && m.getEnd().isBefore(cutoffDate))
            .toList();

        log.info("Trouvé {} réunions à traiter pour archivage/purge", oldMeetings.size());

        for (Meeting meeting : oldMeetings) {
            try {
                archiveAndPurgeMeeting(meeting);
            } catch (Exception e) {
                log.error("Erreur lors du traitement de la réunion {}: {}", meeting.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Archive et purge une réunion spécifique
     * Seules les réunions avec données d'assistance sont archivées.
     * Les autres sont simplement supprimées.
     * @param meeting La réunion à archiver/purger
     */
    @Transactional
    public void archiveAndPurgeMeeting(Meeting meeting) {
        log.debug("Archivage et purge de la réunion {}", meeting.getId());

        // Récupère les données d'assistance
        MeetingAssistance assistance = meetingAssistanceRepository
            .findByMeetingId(meeting.getId())
            .orElse(null);

        if (assistance != null) {
            // Archive uniquement si assistance existe
            MeetingArchive archive = createArchive(meeting, assistance);
            meetingArchiveRepository.save(archive);
            log.debug("Réunion {} archivée avec ID d'archive {}", meeting.getId(), archive.getId());
        } else {
            // Pas d'assistance = pas d'archivage, juste suppression
            log.debug("Pas de donnée d'assistance pour la réunion {} - simple suppression", meeting.getId());
        }

        // Purge les données détaillées
        purgeDetailedData(meeting);
    }

    /**
     * Crée un enregistrement d'archive à partir d'une réunion et ses données d'assistance
     * @param meeting La réunion
     * @param assistance Les données d'assistance
     * @return L'archive créée
     */
    private MeetingArchive createArchive(Meeting meeting, MeetingAssistance assistance) {
        return new MeetingArchive(
            meeting.getId(),
            meeting,
            assistance.getInPersonTotal(),
            assistance.getTotal() - assistance.getInPersonTotal()
        );
    }

    /**
     * Supprime les données détaillées d'une réunion
     * Supprime les participants et les données d'assistance, mais conserve l'enregistrement
     * de base de la réunion avec une date de purge
     * @param meeting La réunion
     */
    private void purgeDetailedData(Meeting meeting) {
        log.debug("Suppression des données détaillées de la réunion {}", meeting.getId());

        // Supprime les données d'assistance (supprime aussi les assistance_values en cascade)
        meetingAssistanceRepository.deleteByMeetingId(meeting.getId());

        // Supprime les participants
        participantRepository.deleteByMeetingId(meeting.getId());

        // Supprime la réunion elle-même
        meetingRepository.delete(meeting);

        log.debug("Réunion {} et ses données détaillées supprimées", meeting.getId());
    }

    /**
     * Récupère les statistiques d'archivage
     * @return Un objet contenant les statistiques
     */
    public ArchiveStats getArchiveStats() {
        LocalDateTime last90Days = LocalDateTime.now().minusDays(90);
        long archivedCount = meetingArchiveRepository.countByArchivedAtAfter(last90Days);
        long totalArchived = meetingArchiveRepository.count();

        return new ArchiveStats(archivedCount, totalArchived);
    }

    /**
     * Classe interne pour les statistiques d'archivage
     */
    public static class ArchiveStats {
        public long last90DaysCount;
        public long totalCount;

        public ArchiveStats(long last90DaysCount, long totalCount) {
            this.last90DaysCount = last90DaysCount;
            this.totalCount = totalCount;
        }
    }
}
