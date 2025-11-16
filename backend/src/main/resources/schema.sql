-- Table pour stocker les données d'assistance par meeting
CREATE TABLE IF NOT EXISTS meeting_assistance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL UNIQUE,
    total INTEGER NOT NULL,
    in_person_total INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (meeting_id) REFERENCES meetings(id) ON DELETE CASCADE
);

-- Table pour stocker les valeurs d'assistance par participantId
CREATE TABLE IF NOT EXISTS assistance_values (
    meeting_assistance_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    assistance_value INTEGER NOT NULL,
    PRIMARY KEY (meeting_assistance_id, participant_id),
    FOREIGN KEY (meeting_assistance_id) REFERENCES meeting_assistance(id) ON DELETE CASCADE,
    FOREIGN KEY (participant_id) REFERENCES participants(id) ON DELETE CASCADE
);
