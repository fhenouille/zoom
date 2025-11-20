package com.zoom.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entité représentant un archivage de réunion.
 * Stocke les données agrégées des réunions avec assistance enregistrée.
 * Seules les réunions avec données d'assistance sont archivées.
 */
@Entity
@Table(name = "meeting_archive")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingArchive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Référence à la réunion
    @Column(name = "meeting_id", nullable = false, unique = true)
    private Long meetingId;

    // Date et heure de début
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    // Date et heure de fin
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    // Fuseau horaire
    @Column(name = "timezone")
    private String timezone;

    // Nombre de participants présentiels
    @Column(name = "in_person_total", nullable = false)
    private Integer inPersonTotal;

    // Nombre de participants en visio
    @Column(name = "remote_total", nullable = false)
    private Integer remoteTotal;

    // Date et heure d'archivage
    @Column(name = "archived_at", nullable = false)
    private LocalDateTime archivedAt;

    /**
     * Crée une instance MeetingArchive à partir d'une réunion et ses statistiques
     */
    public MeetingArchive(Long meetingId, Meeting meeting, Integer inPersonTotal, Integer remoteTotal) {
        this.meetingId = meetingId;
        this.startTime = meeting.getStart();
        this.endTime = meeting.getEnd();
        this.timezone = meeting.getTimezone();
        this.inPersonTotal = inPersonTotal;
        this.remoteTotal = remoteTotal;
        this.archivedAt = LocalDateTime.now();
    }

    /**
     * Retourne le nombre total de participants
     */
    public Integer getTotalParticipants() {
        return inPersonTotal + remoteTotal;
    }
}

