-- Active l'extension PostGIS (types et fonctions géospatiaux).
--
-- Les tables métier sont introduites par les modules à partir de l'issue #9,
-- chacune via ses propres migrations versionnées ajoutées à ce dossier.
CREATE EXTENSION IF NOT EXISTS postgis;
