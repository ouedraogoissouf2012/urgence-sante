-- Identifiants du portail hospitalier (issue #42).
-- Le jeton en clair n'est JAMAIS stocké : seule son empreinte SHA-256 (hex)
-- est conservée. Un opérateur est lié à un établissement (portée), un
-- administrateur (facility_id NULL) peut agir sur tous les établissements.
CREATE TABLE portal_credential (
    id           UUID PRIMARY KEY,
    label        TEXT NOT NULL,
    token_hash   TEXT NOT NULL UNIQUE,
    role         TEXT NOT NULL CHECK (role IN ('FACILITY_OPERATOR', 'ADMIN')),
    facility_id  UUID,
    active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- Un opérateur DOIT être rattaché à un établissement ; un admin, non.
    CONSTRAINT chk_operator_scope CHECK (
        (role = 'FACILITY_OPERATOR' AND facility_id IS NOT NULL)
        OR (role = 'ADMIN' AND facility_id IS NULL)
    )
);

CREATE INDEX idx_portal_credential_token ON portal_credential (token_hash) WHERE active;
