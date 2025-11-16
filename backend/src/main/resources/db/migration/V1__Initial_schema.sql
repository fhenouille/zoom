-- Initial schema creation for zoom database
-- This migration creates all the base tables

CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS meetings (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    zoom_id VARCHAR(255) NOT NULL UNIQUE,
    zoom_uuid VARCHAR(255),
    topic VARCHAR(255) NOT NULL,
    start TIMESTAMP,
    end TIMESTAMP,
    duration_minutes INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS participants (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    user_id VARCHAR(255),
    name VARCHAR(255) NOT NULL,
    duration_minutes INTEGER DEFAULT 0,
    join_time TIMESTAMP,
    leave_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_participants_meeting FOREIGN KEY (meeting_id) REFERENCES meetings(id) ON DELETE CASCADE,
    UNIQUE(meeting_id, user_id)
);

CREATE TABLE IF NOT EXISTS meeting_assistance (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    meeting_id BIGINT NOT NULL UNIQUE,
    total INTEGER NOT NULL,
    in_person_total INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_meeting_assistance_meeting FOREIGN KEY (meeting_id) REFERENCES meetings(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS assistance_values (
    meeting_assistance_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    assistance_value INTEGER NOT NULL,
    PRIMARY KEY (meeting_assistance_id, participant_id),
    CONSTRAINT fk_assistance_values_meeting_assistance FOREIGN KEY (meeting_assistance_id) REFERENCES meeting_assistance(id) ON DELETE CASCADE,
    CONSTRAINT fk_assistance_values_participant FOREIGN KEY (participant_id) REFERENCES participants(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_meetings_zoom_id ON meetings(zoom_id);
CREATE INDEX idx_meetings_zoom_uuid ON meetings(zoom_uuid);
CREATE INDEX idx_participants_meeting_id ON participants(meeting_id);
CREATE INDEX idx_participants_user_id ON participants(user_id);
CREATE INDEX idx_meeting_assistance_meeting_id ON meeting_assistance(meeting_id);
CREATE INDEX idx_assistance_values_meeting_id ON assistance_values(meeting_assistance_id);
