-- Trace d'audit persistante (issue #136). Une ligne par événement métier
-- consommé ; l'identité de la ligne EST celle de l'événement source
-- (source_event_id), ce qui rend l'insertion idempotente face à une
-- livraison au moins une fois (voir availability_outbox / OutboxRelay).
CREATE TABLE audit_entry (
    source_event_id UUID PRIMARY KEY,
    action          TEXT NOT NULL,
    resource_type   TEXT NOT NULL,
    resource_id     TEXT NOT NULL,
    actor_id        TEXT,
    actor_label     TEXT,
    correlation_id  TEXT NOT NULL,
    occurred_at     TIMESTAMPTZ NOT NULL,
    recorded_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_entry_resource ON audit_entry (resource_type, resource_id);
CREATE INDEX idx_audit_entry_occurred_at ON audit_entry (occurred_at);
