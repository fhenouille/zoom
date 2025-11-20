package com.zoom.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zoom.entity.MeetingArchive;

/**
 * Repository pour l'entité MeetingArchive
 */
@Repository
public interface MeetingArchiveRepository extends JpaRepository<MeetingArchive, Long> {

    /**
     * Trouve toutes les réunions archivées dans une plage de dates
     */
    List<MeetingArchive> findByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Trouve toutes les réunions archivées après une date donnée
     */
    List<MeetingArchive> findByArchivedAtAfter(LocalDateTime archivedAt);

    /**
     * Compte les réunions archivées
     */
    Long countByArchivedAtAfter(LocalDateTime archivedAt);

    /**
     * Supprime les archives de réunion plus anciennes qu'une date donnée
     */
    @Query("DELETE FROM MeetingArchive ma WHERE ma.archivedAt < :cutoffDate")
    void deleteArchivedBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}

