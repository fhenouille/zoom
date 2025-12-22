package com.zoom.dto;

import java.time.LocalDateTime;

import lombok.*;

/**
 * DTO représentant une réunion avec les données d'assistance sauvegardées
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingWithAssistance {

    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private String topic;
    private String hostName;
    private String hostEmail;
    private Integer duration;
    private String timezone;

    // Données d'assistance sauvegardées (null si pas encore sauvegardées)
    private Integer inPersonTotal;
    private Integer videoconferenceTotal;
}
