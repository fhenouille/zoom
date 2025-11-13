package com.zoom.service;

import com.zoom.entity.Meeting;
import com.zoom.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion des réunions
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MeetingService {

    private final MeetingRepository meetingRepository;

    /**
     * Récupère toutes les réunions
     */
    @Transactional(readOnly = true)
    public List<Meeting> getAllMeetings() {
        log.info("Récupération de toutes les réunions");
        return meetingRepository.findAll();
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
