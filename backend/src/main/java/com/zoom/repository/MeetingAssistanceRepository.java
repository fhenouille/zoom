package com.zoom.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zoom.entity.MeetingAssistance;

@Repository
public interface MeetingAssistanceRepository extends JpaRepository<MeetingAssistance, Long> {

    Optional<MeetingAssistance> findByMeetingId(Long meetingId);

    void deleteByMeetingId(Long meetingId);
}
