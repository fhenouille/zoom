package com.zoom.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Entité représentant une réunion
 */
@Entity
@Table(name = "meetings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La date de début est obligatoire")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime start;

    @NotNull(message = "La date de fin est obligatoire")
    @Column(name = "end_time", nullable = false)
    private LocalDateTime end;

    // Identifiant du meeting Zoom (peut être partagé par plusieurs sessions/instances)
    @Column(name = "zoom_meeting_id")
    private String zoomMeetingId;

    // UUID unique de la session/instance Zoom (clé unique)
    @Column(name = "zoom_uuid", unique = true)
    private String zoomUuid;

    // Sujet/titre de la réunion
    @Column(name = "topic")
    private String topic;

    // Type de réunion (1=instant, 2=scheduled, etc.)
    @Column(name = "type")
    private Integer type;

    // Durée en minutes
    @Column(name = "duration")
    private Integer duration;

    // Fuseau horaire
    @Column(name = "timezone")
    private String timezone;

    // Nom de l'hôte
    @Column(name = "host_name")
    private String hostName;

    // Email de l'hôte
    @Column(name = "host_email")
    private String hostEmail;
}
