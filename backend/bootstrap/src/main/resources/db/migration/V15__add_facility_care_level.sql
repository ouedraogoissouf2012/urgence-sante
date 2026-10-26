-- Niveau de soins de chaque établissement (pyramide sanitaire), ADR-008.
-- Demande client : un centre de 1er niveau ne traite pas les urgences et n'est
-- JAMAIS proposé par l'orientation ; 2e et 3e niveaux le sont. La règle vit
-- dans le domaine (CareLevel) ; la base garantit seulement que chaque
-- établissement porte un niveau valide.
--
-- AUCUNE valeur par défaut : un chemin d'écriture qui oublierait le niveau
-- échoue bruyamment au lieu de classer le centre en silence (l'import exige le
-- champ, le jeu de démonstration le déclare).
ALTER TABLE facility ADD COLUMN care_level TEXT;

-- 1. Jeu de démarrage réel (infrastructure/directory/abidjan-starter.json) :
--    niveau connu, confirmé par le tableau du client pour le CHU de Treichville
--    (3e niveau) et l'Hôpital Général de Port-Bouët (2e niveau). Le réimport au
--    démarrage (profil production) réaffirme ensuite ces valeurs depuis le JSON.
UPDATE facility SET care_level = 'TERTIARY'
 WHERE source = 'annuaire-public-ci'
   AND external_ref IN ('chu-cocody', 'chu-treichville', 'chu-yopougon');

UPDATE facility SET care_level = 'SECONDARY'
 WHERE source = 'annuaire-public-ci'
   AND external_ref IN ('hopital-general-abobo', 'hopital-general-port-bouet');

-- 2. Données de démonstration (profil local) : remplacées juste après par la
--    migration répétable R__seed_demo (qui déclare leur niveau), ou purgées au
--    démarrage en production. Valeur transitoire sans conséquence.
UPDATE facility SET care_level = 'PRIMARY'
 WHERE care_level IS NULL AND data_status = 'DEMO';

-- 3. Tout autre établissement : niveau INCONNU. Choix conservateur — classé
--    PRIMARY, donc jamais proposé pour une urgence tant qu'il n'est pas
--    reclassé par un import —, et SIGNALÉ dans le journal de migration plutôt
--    que passé sous silence. Aucun cas attendu : l'annuaire de production ne
--    contient que le jeu de démarrage traité en 1.
DO $$
DECLARE
    unclassified INTEGER;
BEGIN
    UPDATE facility SET care_level = 'PRIMARY' WHERE care_level IS NULL;
    GET DIAGNOSTICS unclassified = ROW_COUNT;
    IF unclassified > 0 THEN
        RAISE WARNING 'V15 : % établissement(s) sans niveau connu classé(s) PRIMARY, jamais proposé(s) pour une urgence : à reclasser par import (careLevel).', unclassified;
    END IF;
END $$;

ALTER TABLE facility
    ALTER COLUMN care_level SET NOT NULL,
    ADD CONSTRAINT chk_facility_care_level
        CHECK (care_level IN ('PRIMARY', 'SECONDARY', 'TERTIARY'));
