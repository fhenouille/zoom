-- Table pour stocker les données d'assistance par meeting
CREATE TABLE IF NOT EXISTS meeting_assistance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL UNIQUE,
    total INTEGER NOT NULL,
    FOREIGN KEY (meeting_id) REFERENCES meetings(id) ON DELETE CASCADE
);

-- Table pour stocker les valeurs d'assistance (liste ordonnée)
CREATE TABLE IF NOT EXISTS assistance_values (
    meeting_assistance_id BIGINT NOT NULL,
    position INTEGER NOT NULL,
    assistance_value INTEGER NOT NULL,
    PRIMARY KEY (meeting_assistance_id, position),
    FOREIGN KEY (meeting_assistance_id) REFERENCES meeting_assistance(id) ON DELETE CASCADE
);
