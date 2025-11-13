package com.zoom.service;

import com.zoom.entity.Meeting;
import com.zoom.repository.MeetingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour MeetingService
 */
@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @InjectMocks
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
    void getAllMeetings_ShouldReturnListOfMeetings() {
        // Arrange
        List<Meeting> meetings = Arrays.asList(testMeeting);
        when(meetingRepository.findAll()).thenReturn(meetings);

        // Act
        List<Meeting> result = meetingService.getAllMeetings();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(meetingRepository, times(1)).findAll();
    }

    @Test
    void getMeetingById_WithValidId_ShouldReturnMeeting() {
        // Arrange
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(testMeeting));

        // Act
        Optional<Meeting> result = meetingService.getMeetingById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testMeeting.getId(), result.get().getId());
        verify(meetingRepository, times(1)).findById(1L);
    }

    @Test
    void createMeeting_WithValidMeeting_ShouldSaveMeeting() {
        // Arrange
        when(meetingRepository.save(any(Meeting.class))).thenReturn(testMeeting);

        // Act
        Meeting result = meetingService.createMeeting(testMeeting);

        // Assert
        assertNotNull(result);
        assertEquals(testMeeting.getId(), result.getId());
        verify(meetingRepository, times(1)).save(any(Meeting.class));
    }

    @Test
    void createMeeting_WithEndBeforeStart_ShouldThrowException() {
        // Arrange
        testMeeting.setEnd(LocalDateTime.of(2025, 11, 14, 8, 0));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            meetingService.createMeeting(testMeeting);
        });
    }

    @Test
    void deleteMeeting_ShouldCallRepository() {
        // Act
        meetingService.deleteMeeting(1L);

        // Assert
        verify(meetingRepository, times(1)).deleteById(1L);
    }

    @Test
    void getUpcomingMeetings_ShouldReturnFutureMeetings() {
        // Arrange
        List<Meeting> futureMeetings = Arrays.asList(testMeeting);
        when(meetingRepository.findByStartAfter(any(LocalDateTime.class)))
                .thenReturn(futureMeetings);

        // Act
        List<Meeting> result = meetingService.getUpcomingMeetings();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(meetingRepository, times(1)).findByStartAfter(any(LocalDateTime.class));
    }
}
