package com.zoom.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * DTO représentant un participant Zoom dans une session
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoomParticipant {

    @JsonProperty("id")
    private String id;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("user_email")
    private String userEmail;

    @JsonProperty("join_time")
    private String joinTime;

    @JsonProperty("leave_time")
    private String leaveTime;

    @JsonProperty("duration")
    private Integer duration; // en secondes
}
