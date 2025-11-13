package com.zoom.repository;

import com.zoom.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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
}
