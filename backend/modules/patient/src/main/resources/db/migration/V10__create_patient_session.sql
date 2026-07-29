-- Sessions patient. Le jeton porteur en clair n'est JAMAIS stocké : seule son
-- empreinte SHA-256 (hex) est conservée. Un compte peut avoir plusieurs
-- sessions (plusieurs appareils) ; chacune expire à expires_at.
CREATE TABLE patient_session (
    id          UUID PRIMARY KEY,
    patient_id  UUID NOT NULL REFERENCES patient_account (id) ON DELETE CASCADE,
    token_hash  TEXT NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_patient_session_token ON patient_session (token_hash);
CREATE INDEX idx_patient_session_patient ON patient_session (patient_id);
