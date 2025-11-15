package com.zoom.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * DTO représentant la réponse paginée de l'API Zoom pour les participants
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoomParticipantResponse {

    @JsonProperty("page_count")
    private Integer pageCount;

    @JsonProperty("page_size")
    private Integer pageSize;

    @JsonProperty("total_records")
    private Integer totalRecords;

    @JsonProperty("next_page_token")
    private String nextPageToken;

    @JsonProperty("participants")
    private List<ZoomParticipant> participants;
}
