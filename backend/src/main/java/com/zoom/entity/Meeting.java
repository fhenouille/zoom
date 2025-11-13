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
}
