package com.zoom.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zoom.entity.Meeting;
import com.zoom.service.MeetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour MeetingController
 */
@WebMvcTest(MeetingController.class)
class MeetingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MeetingService meetingService;

    private Meeting testMeeting;

    @BeforeEach
    void setUp() {
        testMeeting = new Meeting();
        testMeeting.setId(1L);
        testMeeting.setStart(LocalDateTime.of(2025, 11, 14, 9, 0));
        testMeeting.setEnd(LocalDateTime.of(2025, 11, 14, 10, 0));
    }

    @Test
    void getAllMeetings_ShouldReturnListOfMeetings() throws Exception {
        // Arrange
        when(meetingService.getAllMeetings()).thenReturn(Arrays.asList(testMeeting));

        // Act & Assert
        mockMvc.perform(get("/api/meetings"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].start").exists())
                .andExpect(jsonPath("$[0].end").exists());
    }

    @Test
    void getMeetingById_WithExistingId_ShouldReturnMeeting() throws Exception {
        // Arrange
        when(meetingService.getMeetingById(1L)).thenReturn(Optional.of(testMeeting));

        // Act & Assert
        mockMvc.perform(get("/api/meetings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getMeetingById_WithNonExistingId_ShouldReturn404() throws Exception {
        // Arrange
        when(meetingService.getMeetingById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/meetings/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createMeeting_WithValidData_ShouldReturn201() throws Exception {
        // Arrange
        when(meetingService.createMeeting(any(Meeting.class))).thenReturn(testMeeting);

        // Act & Assert
        mockMvc.perform(post("/api/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testMeeting)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }
}
