-- Outbox transactionnel des événements de disponibilité (issue #43).
-- L'événement est écrit DANS LA MÊME TRANSACTION que la disponibilité courante
-- et l'historique : une panne de publication ne peut pas le perdre. Un relais
-- publie puis marque published_at ; les échecs incrémentent attempts et
-- restent en attente (reprise). Livraison AU MOINS UNE FOIS : event_id permet
-- la déduplication côté consommateur.
CREATE TABLE availability_outbox (
    event_id       UUID PRIMARY KEY,
    facility_id    UUID NOT NULL,
    service_code   TEXT NOT NULL,
    status         TEXT NOT NULL,
    updated_at     TIMESTAMPTZ NOT NULL,
    correlation_id TEXT NOT NULL,
    occurred_at    TIMESTAMPTZ NOT NULL,
    published_at   TIMESTAMPTZ,
    attempts       INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_availability_outbox_pending
    ON availability_outbox (occurred_at) WHERE published_at IS NULL;
