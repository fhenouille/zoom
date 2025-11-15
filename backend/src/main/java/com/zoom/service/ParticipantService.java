package com.zoom.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zoom.dto.ZoomParticipant;
import com.zoom.entity.Meeting;
import com.zoom.entity.Participant;
import com.zoom.repository.MeetingRepository;
import com.zoom.repository.ParticipantRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service pour la gestion des participants aux meetings
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final MeetingRepository meetingRepository;
    private final ZoomApiService zoomApiService;

    /**
     * Récupère les participants d'un meeting
     * Si non présents en base, les récupère depuis l'API Zoom et les sauvegarde
     */
    @Transactional
    public List<Participant> getParticipants(Long meetingId) {
        log.info("👥 Récupération des participants pour le meeting ID: {}", meetingId);

        // Vérifie si le meeting existe
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting introuvable: " + meetingId));

        // Vérifie si on a déjà les participants en base
        if (participantRepository.existsByMeetingId(meetingId)) {
            log.info("✓ Participants déjà en base pour le meeting {}", meetingId);
            return participantRepository.findByMeetingId(meetingId);
        }

        // Sinon, récupère depuis Zoom
        log.info("🔄 Synchronisation des participants depuis Zoom pour le meeting {}", meetingId);
        syncParticipantsFromZoom(meeting);

        return participantRepository.findByMeetingId(meetingId);
    }

    /**
     * Synchronise les participants depuis l'API Zoom
     * Calcule le cumul de présence pour chaque utilisateur
     */
    private void syncParticipantsFromZoom(Meeting meeting) {
        try {
            // Utilisons l'UUID (identifiant unique de la session)
            String meetingIdentifier = meeting.getZoomUuid();
            log.info("🔍 Utilisation de l'UUID: {}", meetingIdentifier);

            // Récupère les participants depuis Zoom
            List<ZoomParticipant> zoomParticipants = zoomApiService.getMeetingParticipants(meetingIdentifier);

            log.info("📥 {} enregistrements de participation reçus de Zoom", zoomParticipants.size());

            // Regroupe par utilisateur et calcule le cumul de présence
            Map<String, List<ZoomParticipant>> participantsByUser = zoomParticipants.stream()
                    .collect(Collectors.groupingBy(p -> p.getUserId() != null ? p.getUserId() : p.getName()));

            log.info("👤 {} utilisateurs uniques identifiés", participantsByUser.size());

            List<Participant> participants = new ArrayList<>();

            for (Map.Entry<String, List<ZoomParticipant>> entry : participantsByUser.entrySet()) {
                String userId = entry.getKey();
                List<ZoomParticipant> userConnections = entry.getValue();

                // Calcule la durée totale (somme de toutes les connexions)
                int totalDurationSeconds = userConnections.stream()
                        .mapToInt(p -> p.getDuration() != null ? p.getDuration() : 0)
                        .sum();

                int totalDurationMinutes = totalDurationSeconds / 60;

                // Prend les infos du premier enregistrement (nom, email)
                ZoomParticipant firstConnection = userConnections.get(0);

                // Trouve la première connexion et la dernière déconnexion
                String firstJoinTime = userConnections.stream()
                        .map(ZoomParticipant::getJoinTime)
                        .filter(Objects::nonNull)
                        .min(String::compareTo)
                        .orElse(null);

                String lastLeaveTime = userConnections.stream()
                        .map(ZoomParticipant::getLeaveTime)
                        .filter(Objects::nonNull)
                        .max(String::compareTo)
                        .orElse(null);

                // Crée l'entité Participant
                Participant participant = new Participant();
                participant.setMeeting(meeting);
                participant.setUserId(userId);
                participant.setName(firstConnection.getName());
                participant.setDurationMinutes(totalDurationMinutes);
                participant.setJoinTime(firstJoinTime);
                participant.setLeaveTime(lastLeaveTime);

                participants.add(participant);

                log.debug("  ✓ {} - Durée totale: {}min (sur {} connexion(s))",
                    firstConnection.getName(), totalDurationMinutes, userConnections.size());
            }

            // Sauvegarde tous les participants
            participantRepository.saveAll(participants);
            log.info("✅ {} participants sauvegardés pour le meeting {}", participants.size(), meeting.getId());

        } catch (Exception e) {
            log.error("❌ Erreur lors de la synchronisation des participants: {}", e.getMessage());
            log.debug("Stack trace:", e);
            throw new RuntimeException("Erreur lors de la synchronisation des participants: " + e.getMessage(), e);
        }
    }

    /**
     * Force la re-synchronisation des participants depuis Zoom
     */
    @Transactional
    public List<Participant> refreshParticipants(Long meetingId) {
        log.info("🔄 Re-synchronisation forcée des participants pour le meeting {}", meetingId);

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("Meeting introuvable: " + meetingId));

        // Supprime les participants existants et flush pour forcer l'exécution immédiate
        if (participantRepository.existsByMeetingId(meetingId)) {
            participantRepository.deleteByMeetingId(meetingId);
            participantRepository.flush(); // Force l'exécution du DELETE avant de continuer
            log.info("🗑️ Participants existants supprimés");
        }

        // Re-synchronise
        syncParticipantsFromZoom(meeting);

        return participantRepository.findByMeetingId(meetingId);
    }
}
