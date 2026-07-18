-- Intégrité référentielle transverse (issue #37).
--
-- Décisions :
-- 1. facility_service.service_code doit exister au catalogue medical_service.
-- 2. La disponibilité COURANTE référence le couple (établissement, service)
--    réellement offert : supprimée en cascade si l'offre disparaît.
-- 3. availability_history est un JOURNAL D'AUDIT : volontairement SANS clé
--    étrangère, il survit aux suppressions d'établissements ou de services
--    (traçabilité). Sa cohérence est garantie à l'écriture par l'application.

-- Nettoyage défensif d'éventuels orphelins avant pose des contraintes.
DELETE FROM facility_service fs
WHERE NOT EXISTS (SELECT 1 FROM medical_service ms WHERE ms.code = fs.service_code);

DELETE FROM availability a
WHERE NOT EXISTS (
    SELECT 1 FROM facility_service fs
    WHERE fs.facility_id = a.facility_id AND fs.service_code = a.service_code);

ALTER TABLE facility_service
    ADD CONSTRAINT fk_facility_service_catalog
        FOREIGN KEY (service_code) REFERENCES medical_service (code);

ALTER TABLE availability
    ADD CONSTRAINT fk_availability_offered_service
        FOREIGN KEY (facility_id, service_code)
            REFERENCES facility_service (facility_id, service_code)
            ON DELETE CASCADE;
