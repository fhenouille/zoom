package com.zoom.dto;

/**
 * DTO pour envoyer les données d'assistance au frontend
 */
public class ParticipantWithAssistance {
    private Long id;
    private String userId;
    private String name;
    private Integer durationMinutes;
    private String joinTime;
    private String leaveTime;
    private Integer assistanceValue;

    // Constructeurs
    public ParticipantWithAssistance() {
    }

    public ParticipantWithAssistance(Long id, String userId, String name, Integer durationMinutes,
                                    String joinTime, String leaveTime, Integer assistanceValue) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.durationMinutes = durationMinutes;
        this.joinTime = joinTime;
        this.leaveTime = leaveTime;
        this.assistanceValue = assistanceValue;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(String joinTime) {
        this.joinTime = joinTime;
    }

    public String getLeaveTime() {
        return leaveTime;
    }

    public void setLeaveTime(String leaveTime) {
        this.leaveTime = leaveTime;
    }

    public Integer getAssistanceValue() {
        return assistanceValue;
    }

    public void setAssistanceValue(Integer assistanceValue) {
        this.assistanceValue = assistanceValue;
    }
}
