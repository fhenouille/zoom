package com.zoom.dto;

import java.util.List;

/**
 * DTO pour recevoir les données d'assistance à sauvegarder
 */
public class AssistanceSaveRequest {
    private Integer total;
    private Integer inPersonTotal;
    private List<Integer> values;

    // Constructeurs
    public AssistanceSaveRequest() {
    }

    public AssistanceSaveRequest(Integer total, Integer inPersonTotal, List<Integer> values) {
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

    public List<Integer> getValues() {
        return values;
    }

    public void setValues(List<Integer> values) {
        this.values = values;
    }
}
