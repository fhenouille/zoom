package com.zoom.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;

/**
 * Réponse contenant les statistiques d'assistance sur une période donnée
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssistanceStatisticsResponse {

    private List<DailyAssistanceStats> dailyStats;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    /**
     * Statistiques pour une journée donnée
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyAssistanceStats {
        private String date; // Format ISO (YYYY-MM-DD)
        private Integer inPerson; // Nombre de personnes en présentiel
        private Integer remote; // Nombre de personnes en visio (total - inPerson)
        private Integer total; // Total d'assistance
        private Integer meetingCount; // Nombre de réunions ce jour-là
    }
}
