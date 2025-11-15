package com.zoom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Réponse de l'API Zoom pour les résultats de sondages
 * Note: Le champ 'questions' de l'API Zoom contient en fait les participants avec leurs réponses
 */
@Getter
@Setter
public class ZoomPollResponse {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("uuid")
    private String uuid;
    
    @JsonProperty("start_time")
    private String startTime;
    
    // Désérialisation depuis Zoom: utilise "questions"
    @JsonProperty(value = "questions", access = JsonProperty.Access.WRITE_ONLY)
    private List<ZoomPollResult> questionsData;
    
    // Sérialisation vers frontend: utilise "participants"
    @JsonProperty("participants")
    public List<ZoomPollResult> getParticipants() {
        return questionsData;
    }
    
    public void setParticipants(List<ZoomPollResult> participants) {
        this.questionsData = participants;
    }
}
