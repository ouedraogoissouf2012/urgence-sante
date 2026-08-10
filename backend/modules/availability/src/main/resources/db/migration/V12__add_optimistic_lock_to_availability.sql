-- Ajoute la colonne version pour le verrouillage optimiste (issue #133).
-- Une écriture obsolète (version périmée) sera rejetée au lieu de l'emporter silencieusement.
ALTER TABLE availability ADD COLUMN version INTEGER NOT NULL DEFAULT 0;
