-- Migration V3: Add data archival and purge functionality
-- This migration adds archival tables to preserve meeting statistics
-- while allowing purge of detailed participant data after 90 days

-- Create meeting_archive table to store aggregated meeting data
-- Only meetings with assistance records are archived
CREATE TABLE IF NOT EXISTS meeting_archive (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    meeting_id BIGINT NOT NULL UNIQUE,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    timezone VARCHAR(100),
    in_person_total INTEGER NOT NULL,
    remote_total INTEGER NOT NULL,
    archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_meeting_archive_meeting FOREIGN KEY (meeting_id) REFERENCES meetings(id) ON DELETE CASCADE
);

-- Add index for querying archived meetings
CREATE INDEX idx_meeting_archive_start_time ON meeting_archive(start_time);
CREATE INDEX idx_meeting_archive_archived_at ON meeting_archive(archived_at);
CREATE INDEX idx_meeting_archive_meeting_id ON meeting_archive(meeting_id);
