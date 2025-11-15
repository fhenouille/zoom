package com.zoom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zoom.entity.Participant;

/**
 * Repository pour l'entité Participant
 */
@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    /**
     * Trouve tous les participants d'un meeting
     */
    List<Participant> findByMeetingId(Long meetingId);

    /**
     * Vérifie si des participants existent pour un meeting
     */
    boolean existsByMeetingId(Long meetingId);

    /**
     * Supprime tous les participants d'un meeting
     */
    void deleteByMeetingId(Long meetingId);
}
