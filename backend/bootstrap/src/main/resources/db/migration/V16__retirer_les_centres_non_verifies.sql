-- Règle d'annuaire : une donnée douteuse est RETIRÉE, jamais conservée « en
-- attendant » (infrastructure/directory/SOURCES.md). Ces 4 centres, ajoutés
-- depuis le tableau client (#173), n'ont pas pu être vérifiés :
--   - Clinique Sainte Marie Port-Bouët : aucun service d'urgence attesté
--     publiquement — or elle était PROPOSÉE aux patients (3e niveau) ;
--   - Centre Médical Victoria, Centre de santé rural CNRA : position
--     approximative ;
--   - Médico-scolaire de Port-Bouët : une seule source, sans type d'établissement.
--
-- L'import au démarrage ne fait qu'insérer ou mettre à jour (upsert) : retirer
-- une ligne du fichier ne l'efface pas de la base — d'où cette migration.
-- Clé naturelle (source, external_ref) : aucun autre établissement touché.
-- facility_service et availability suivent par ON DELETE CASCADE (V2, V5).
DELETE FROM facility
 WHERE source = 'tableau-client-port-bouet'
   AND external_ref IN (
       'clinique-sainte-marie-port-bouet',
       'centre-medical-victoria',
       'csr-cnra-port-bouet',
       'medico-scolaire-port-bouet');
