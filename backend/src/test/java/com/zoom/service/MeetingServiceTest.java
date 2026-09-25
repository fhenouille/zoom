package com.zoom.service;

import com.zoom.entity.Meeting;
import com.zoom.entity.MeetingAssistance;
import com.zoom.repository.MeetingArchiveRepository;
import com.zoom.repository.MeetingAssistanceRepository;
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
import java.util.Map;

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

    @Mock
    private ZoomApiService zoomApiService;

    @Mock
    private MeetingAssistanceRepository meetingAssistanceRepository;

    @Mock
    private MeetingArchiveRepository meetingArchiveRepository;

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

    @Test
    void getAssistanceStatistics_ShouldExcludeZeroAttendanceEntriesAndReturnNote() {
        // Arrange
        Meeting zeroAttendanceMeeting = createMeeting(1L, LocalDateTime.of(2025, 11, 14, 9, 0));
        Meeting attendedMeeting = createMeeting(2L, LocalDateTime.of(2025, 11, 15, 9, 0));

        when(meetingRepository.findByStartBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(zeroAttendanceMeeting, attendedMeeting));
        when(meetingArchiveRepository.findByStartTimeBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(meetingAssistanceRepository.findByMeetingId(1L))
                .thenReturn(Optional.of(new MeetingAssistance(zeroAttendanceMeeting, 5, 0, Map.of())));
        when(meetingAssistanceRepository.findByMeetingId(2L))
                .thenReturn(Optional.of(new MeetingAssistance(attendedMeeting, 5, 3, Map.of())));

        // Act
        var result = meetingService.getAssistanceStatistics(
                LocalDateTime.of(2025, 11, 1, 0, 0),
                LocalDateTime.of(2025, 11, 30, 23, 59));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getDailyStats().size());
        assertEquals("2025-11-15", result.getDailyStats().get(0).getDate());
        assertEquals(3, result.getDailyStats().get(0).getInPerson());
        assertEquals(5, result.getDailyStats().get(0).getRemote());
        assertEquals(8, result.getDailyStats().get(0).getTotal());
        assertEquals(1, result.getExcludedZeroEntries());
        assertNotNull(result.getNote());
        assertTrue(result.getNote().contains("exclues"));
    }

    private Meeting createMeeting(Long id, LocalDateTime start) {
        Meeting meeting = new Meeting();
        meeting.setId(id);
        meeting.setStart(start);
        meeting.setEnd(start.plusHours(1));
        return meeting;
    }
}
