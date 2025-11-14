package com.zoom.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * DTO pour la réponse de l'API Zoom listant les meetings passés
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ZoomMeetingResponse {

    @JsonProperty("from")
    private String from;

    @JsonProperty("to")
    private String to;

    @JsonProperty("page_count")
    private Integer pageCount;

    @JsonProperty("page_size")
    private Integer pageSize;

    @JsonProperty("total_records")
    private Integer totalRecords;

    @JsonProperty("next_page_token")
    private String nextPageToken;

    @JsonProperty("meetings")
    private List<ZoomMeeting> meetings;
}
