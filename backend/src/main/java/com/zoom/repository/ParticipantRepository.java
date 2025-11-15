package com.zoom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
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
     * Utilise @Modifying et @Query pour forcer l'exécution immédiate
     */
    @Modifying
    @Query("DELETE FROM Participant p WHERE p.meeting.id = :meetingId")
    void deleteByMeetingId(@Param("meetingId") Long meetingId);
}
