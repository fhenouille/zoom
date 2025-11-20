package com.zoom.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.zoom.entity.*;
import com.zoom.repository.*;

/**
 * Tests unitaires pour le service DataPurgeService
 */
@ExtendWith(MockitoExtension.class)
public class DataPurgeServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private MeetingAssistanceRepository meetingAssistanceRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private MeetingArchiveRepository meetingArchiveRepository;

    @InjectMocks
    private DataPurgeService dataPurgeService;

    private Meeting testMeeting;
    private MeetingAssistance testAssistance;

    @BeforeEach
    public void setUp() {
        // Crée une réunion de test
        testMeeting = new Meeting();
        testMeeting.setId(1L);
        testMeeting.setTopic("Test Meeting");
        testMeeting.setStart(LocalDateTime.now().minusDays(100));
        testMeeting.setEnd(LocalDateTime.now().minusDays(100).plusHours(1));
        testMeeting.setDuration(60);
        testMeeting.setTimezone("UTC");
        testMeeting.setHostName("Test Host");
        testMeeting.setHostEmail("host@example.com");

        // Crée des données d'assistance de test
        testAssistance = new MeetingAssistance();
        testAssistance.setId(1L);
        testAssistance.setMeeting(testMeeting);
        testAssistance.setTotal(50);
        testAssistance.setInPersonTotal(30);
    }

    @Test
    public void testPurgeOldDataIdentifiesMeetingsOlderThan90Days() {
        // Arrange
        List<Meeting> oldMeetings = new ArrayList<>();
        oldMeetings.add(testMeeting);
        when(meetingRepository.findAll()).thenReturn(oldMeetings);
        when(meetingAssistanceRepository.findByMeetingId(1L)).thenReturn(Optional.of(testAssistance));

        // Act
        dataPurgeService.purgeOldData();

        // Assert
        verify(meetingAssistanceRepository, times(1)).deleteByMeetingId(1L);
        verify(participantRepository, times(1)).deleteByMeetingId(1L);
        verify(meetingRepository, times(1)).save(testMeeting);
        verify(meetingArchiveRepository, times(1)).save(any(MeetingArchive.class));
    }

    @Test
    public void testArchiveAndPurgeMeetingCreatesArchiveWithCorrectData() {
        // Arrange
        when(meetingAssistanceRepository.findByMeetingId(1L)).thenReturn(Optional.of(testAssistance));

        // Act
        dataPurgeService.archiveAndPurgeMeeting(testMeeting);

        // Assert
        verify(meetingArchiveRepository, times(1)).save(argThat(archive ->
            archive.getMeetingId().equals(1L) &&
            archive.getInPersonTotal().equals(30) &&
            archive.getRemoteTotal().equals(20) &&
            archive.getTotalParticipants().equals(50)
        ));
    }

    @Test
    void testPurgeMeetingWithNoAssistanceData() {
        // Arrange
        when(meetingAssistanceRepository.findByMeetingId(1L)).thenReturn(Optional.empty());

        // Act
        dataPurgeService.archiveAndPurgeMeeting(testMeeting);

        // Assert
        // Aucune archive ne doit être créée
        verify(meetingArchiveRepository, never()).save(any());
        // Mais les données détaillées doivent toujours être supprimées
        verify(meetingAssistanceRepository, times(1)).deleteByMeetingId(1L);
        verify(participantRepository, times(1)).deleteByMeetingId(1L);
    }

    @Test
    public void testGetArchiveStatsReturnsCorrectData() {
        // Arrange
        when(meetingArchiveRepository.countByArchivedAtAfter(any(LocalDateTime.class)))
            .thenReturn(10L);
        when(meetingArchiveRepository.count()).thenReturn(100L);

        // Act
        DataPurgeService.ArchiveStats stats = dataPurgeService.getArchiveStats();

        // Assert
        assertEquals(10, stats.last90DaysCount);
        assertEquals(100, stats.totalCount);
    }

    @Test
    public void testArchiveAndPurgeCalculatesRemoteTotalCorrectly() {
        // Arrange
        testAssistance.setTotal(100);
        testAssistance.setInPersonTotal(35);
        when(meetingAssistanceRepository.findByMeetingId(1L)).thenReturn(Optional.of(testAssistance));

        // Act
        dataPurgeService.archiveAndPurgeMeeting(testMeeting);

        // Assert
        verify(meetingArchiveRepository, times(1)).save(argThat(archive ->
            archive.getInPersonTotal().equals(35) &&
            archive.getRemoteTotal().equals(65) &&
            archive.getTotalParticipants().equals(100)
        ));
    }

    @Test
    public void testPurgeMarksReunionAsPurged() {
        // Arrange
        when(meetingAssistanceRepository.findByMeetingId(1L)).thenReturn(Optional.of(testAssistance));

        // Act
        dataPurgeService.archiveAndPurgeMeeting(testMeeting);

        // Assert
        assertNotNull(testMeeting.getPurgeDate());
        verify(meetingRepository, times(1)).save(testMeeting);
    }

    @Test
    public void testPurgeHandlesMultipleMeetings() {
        // Arrange
        Meeting meeting2 = new Meeting();
        meeting2.setId(2L);
        meeting2.setTopic("Meeting 2");
        meeting2.setStart(LocalDateTime.now().minusDays(100));
        meeting2.setEnd(LocalDateTime.now().minusDays(100).plusHours(1));

        List<Meeting> oldMeetings = new ArrayList<>();
        oldMeetings.add(testMeeting);
        oldMeetings.add(meeting2);

        when(meetingRepository.findAll()).thenReturn(oldMeetings);
        when(meetingAssistanceRepository.findByMeetingId(anyLong())).thenReturn(Optional.of(testAssistance));

        // Act
        dataPurgeService.purgeOldData();

        // Assert
        verify(meetingArchiveRepository, times(2)).save(any(MeetingArchive.class));
        verify(meetingAssistanceRepository, times(2)).deleteByMeetingId(anyLong());
        verify(participantRepository, times(2)).deleteByMeetingId(anyLong());
    }
}
