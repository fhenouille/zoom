package com.zoom.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * Représente le résultat d'un participant à un sondage
 */
@Data
public class ZoomPollResult {

    @JsonProperty("email")
    private String email;

    @JsonProperty("name")
    private String name;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("question_details")
    private List<ZoomPollAnswer> questionDetails;
}
