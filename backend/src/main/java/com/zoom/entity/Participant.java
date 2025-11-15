package com.zoom.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entité représentant un participant à une session de meeting
 */
@Entity
@Table(name = "participants",
       uniqueConstraints = @UniqueConstraint(columnNames = {"meeting_id", "user_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relation vers le meeting
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    // Identifiant utilisateur Zoom
    @Column(name = "user_id", nullable = false)
    private String userId;

    // Nom du participant
    @Column(name = "name", nullable = false)
    private String name;

    // Durée totale de présence en minutes (cumul de toutes les connexions)
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    // Heure de première connexion (ISO 8601)
    @Column(name = "join_time")
    private String joinTime;

    // Heure de dernière déconnexion (ISO 8601)
    @Column(name = "leave_time")
    private String leaveTime;
}
