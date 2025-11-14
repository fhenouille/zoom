package com.zoom.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * DTO représentant un meeting Zoom
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoomMeeting {

    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("host_id")
    private String hostId;

    @JsonProperty("topic")
    private String topic;

    @JsonProperty("type")
    private Integer type;

    @JsonProperty("start_time")
    private String startTime;

    @JsonProperty("duration")
    private Integer duration;

    @JsonProperty("timezone")
    private String timezone;

    @JsonProperty("host_email")
    private String hostEmail;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("end_time")
    private String endTime;

    @JsonProperty("participants_count")
    private Integer participantsCount;
}
