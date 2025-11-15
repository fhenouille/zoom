package com.zoom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Représente une réponse à une question de sondage
 */
@Data
public class ZoomPollAnswer {

    @JsonProperty("question")
    private String question;

    @JsonProperty("answer")
    private String answer;

    @JsonProperty("polling_id")
    private String pollingId;

    @JsonProperty("date_time")
    private String dateTime;
}
