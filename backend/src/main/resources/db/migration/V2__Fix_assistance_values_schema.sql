-- Migration: Correction du schéma assistance_values
-- Supprime les anciennes tables d'assistance avec l'ancienne structure position
-- et les recrée avec la nouvelle structure participantId

-- Supprimer la contrainte de clé étrangère sur meeting_assistance
ALTER TABLE meeting_assistance DROP CONSTRAINT IF EXISTS meeting_assistance_meeting_id_key;

-- Supprimer la table assistance_values avec l'ancienne structure
DROP TABLE IF EXISTS assistance_values CASCADE;

-- Supprimer la table meeting_assistance
DROP TABLE IF EXISTS meeting_assistance CASCADE;

-- Recréer les tables avec la nouvelle structure
CREATE TABLE meeting_assistance (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    meeting_id BIGINT NOT NULL UNIQUE,
    total INTEGER NOT NULL,
    in_person_total INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_meeting_assistance_meeting FOREIGN KEY (meeting_id) REFERENCES meetings(id) ON DELETE CASCADE
);

-- Table pour stocker les valeurs d'assistance par participantId
CREATE TABLE assistance_values (
    meeting_assistance_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    assistance_value INTEGER NOT NULL,
    PRIMARY KEY (meeting_assistance_id, participant_id),
    CONSTRAINT fk_assistance_values_meeting_assistance FOREIGN KEY (meeting_assistance_id) REFERENCES meeting_assistance(id) ON DELETE CASCADE,
    CONSTRAINT fk_assistance_values_participant FOREIGN KEY (participant_id) REFERENCES participants(id) ON DELETE CASCADE
);

-- Index pour améliorer les performances
CREATE INDEX idx_meeting_assistance_meeting_id ON meeting_assistance(meeting_id);
CREATE INDEX idx_assistance_values_meeting_id ON assistance_values(meeting_assistance_id);
