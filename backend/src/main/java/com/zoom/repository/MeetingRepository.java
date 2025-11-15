package com.zoom.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zoom.entity.Meeting;

/**
 * Repository pour l'entité Meeting
 */
@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    /**
     * Trouve toutes les réunions entre deux dates
     */
    List<Meeting> findByStartBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Trouve toutes les réunions après une date donnée
     */
    List<Meeting> findByStartAfter(LocalDateTime date);

    /**
     * Trouve toutes les réunions avant une date donnée
     */
    List<Meeting> findByStartBefore(LocalDateTime date);

    /**
     * Trouve une réunion par son ID Zoom
     */
    Optional<Meeting> findByZoomMeetingId(String zoomMeetingId);

    /**
     * Vérifie si une réunion existe avec cet ID Zoom
     */
    boolean existsByZoomMeetingId(String zoomMeetingId);

    /**
     * Trouve une session par son UUID Zoom
     */
    Optional<Meeting> findByZoomUuid(String zoomUuid);

    /**
     * Vérifie si une session existe avec cet UUID Zoom
     */
    boolean existsByZoomUuid(String zoomUuid);
}
