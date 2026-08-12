-- Traçabilité de l'annuaire (issue #41). Chaque établissement porte sa
-- provenance, sa date de vérification, un statut de donnée et un responsable.
-- data_status distingue les données vérifiées, provisoires et de DÉMONSTRATION
-- (fictives) ; ces dernières ne doivent jamais exister en production (garde au
-- démarrage). external_ref + source forment la clé naturelle d'un import
-- idempotent.
ALTER TABLE facility
    ADD COLUMN source       TEXT,
    ADD COLUMN external_ref TEXT,
    ADD COLUMN verified_at  DATE,
    ADD COLUMN steward      TEXT,
    ADD COLUMN data_status  TEXT NOT NULL DEFAULT 'PROVISIONAL'
        CHECK (data_status IN ('VERIFIED', 'PROVISIONAL', 'DEMO'));

-- Clé naturelle d'idempotence : au plus un établissement par (source, réf).
CREATE UNIQUE INDEX uq_facility_source_ref
    ON facility (source, external_ref)
    WHERE source IS NOT NULL AND external_ref IS NOT NULL;

-- Recherche rapide des lignes de démonstration (garde de production).
CREATE INDEX idx_facility_data_status ON facility (data_status);
