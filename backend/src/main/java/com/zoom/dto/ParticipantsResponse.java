package com.zoom.dto;

import java.util.List;

/**
 * DTO pour retourner les participants avec les données d'assistance
 */
public class ParticipantsResponse {
    private List<ParticipantWithAssistance> participants;
    private Integer inPersonTotal;

    // Constructeurs
    public ParticipantsResponse() {
    }

    public ParticipantsResponse(List<ParticipantWithAssistance> participants, Integer inPersonTotal) {
        this.participants = participants;
        this.inPersonTotal = inPersonTotal;
    }

    // Getters et Setters
    public List<ParticipantWithAssistance> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ParticipantWithAssistance> participants) {
        this.participants = participants;
    }

    public Integer getInPersonTotal() {
        return inPersonTotal;
    }

    public void setInPersonTotal(Integer inPersonTotal) {
        this.inPersonTotal = inPersonTotal;
    }
}
