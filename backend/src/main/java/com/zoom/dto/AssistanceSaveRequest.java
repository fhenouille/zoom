package com.zoom.dto;

import java.util.Map;

/**
 * DTO pour recevoir les données d'assistance à sauvegarder
 * Les valeurs sont organisées par participantId (Long -> Integer)
 */
public class AssistanceSaveRequest {
    private Integer total;
    private Integer inPersonTotal;
    private Map<Long, Integer> values;

    // Constructeurs
    public AssistanceSaveRequest() {
    }

    public AssistanceSaveRequest(Integer total, Integer inPersonTotal, Map<Long, Integer> values) {
        this.total = total;
        this.inPersonTotal = inPersonTotal;
        this.values = values;
    }

    // Getters et Setters
    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getInPersonTotal() {
        return inPersonTotal;
    }

    public void setInPersonTotal(Integer inPersonTotal) {
        this.inPersonTotal = inPersonTotal;
    }

    public Map<Long, Integer> getValues() {
        return values;
    }

    public void setValues(Map<Long, Integer> values) {
        this.values = values;
    }
}
