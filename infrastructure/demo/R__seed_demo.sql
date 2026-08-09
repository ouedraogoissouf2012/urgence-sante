-- ============================================================================
-- DONNÉES SIMULÉES — DÉMONSTRATION UNIQUEMENT
-- Les établissements, coordonnées, téléphones et services de ce fichier sont
-- fictifs ou approximatifs. Ils ne doivent JAMAIS servir en production ni
-- être présentés comme des informations médicales réelles.
-- Rechargement idempotent : le jeu est vidé puis réinséré.
-- ============================================================================

TRUNCATE facility_service, availability, availability_history, facility;

-- Toutes les lignes portent data_status = 'DEMO' : fictives, INTERDITES en
-- production (refus à l'import et échec du démarrage en profil production).
INSERT INTO facility (id, name, phone, location, source, external_ref, data_status) VALUES
  ('11111111-0000-0000-0000-000000000001', 'CHU de Cocody [DÉMO]',            '+2250100000001', ST_SetSRID(ST_MakePoint(-3.9851, 5.3496), 4326)::geography, 'demo', 'demo-01', 'DEMO'),
  ('11111111-0000-0000-0000-000000000002', 'CHU de Treichville [DÉMO]',       '+2250100000002', ST_SetSRID(ST_MakePoint(-4.0104, 5.2930), 4326)::geography, 'demo', 'demo-002', 'DEMO'),
  ('11111111-0000-0000-0000-000000000003', 'CHU de Yopougon [DÉMO]',          '+2250100000003', ST_SetSRID(ST_MakePoint(-4.0894, 5.3363), 4326)::geography, 'demo', 'demo-003', 'DEMO'),
  ('11111111-0000-0000-0000-000000000004', 'Hôpital Général d''Abobo [DÉMO]', '+2250100000004', ST_SetSRID(ST_MakePoint(-4.0210, 5.4322), 4326)::geography, 'demo', 'demo-004', 'DEMO'),
  ('11111111-0000-0000-0000-000000000005', 'Hôpital de Marcory [DÉMO]',       '+2250100000005', ST_SetSRID(ST_MakePoint(-3.9820, 5.3010), 4326)::geography, 'demo', 'demo-005', 'DEMO'),
  ('11111111-0000-0000-0000-000000000006', 'PISAM Plateau [DÉMO]',            '+2250100000006', ST_SetSRID(ST_MakePoint(-4.0190, 5.3330), 4326)::geography, 'demo', 'demo-006', 'DEMO'),
  ('11111111-0000-0000-0000-000000000007', 'Clinique d''Adjamé [DÉMO]',       '+2250100000007', ST_SetSRID(ST_MakePoint(-4.0230, 5.3660), 4326)::geography, 'demo', 'demo-007', 'DEMO'),
  ('11111111-0000-0000-0000-000000000008', 'Hôpital de Koumassi [DÉMO]',      '+2250100000008', ST_SetSRID(ST_MakePoint(-3.9530, 5.2890), 4326)::geography, 'demo', 'demo-008', 'DEMO'),
  ('11111111-0000-0000-0000-000000000009', 'Centre de Port-Bouët [DÉMO]',     '+2250100000009', ST_SetSRID(ST_MakePoint(-3.9280, 5.2570), 4326)::geography, 'demo', 'demo-009', 'DEMO'),
  ('11111111-0000-0000-0000-000000000010', 'Clinique de Bingerville [DÉMO]',  '+2250100000010', ST_SetSRID(ST_MakePoint(-3.8850, 5.3560), 4326)::geography, 'demo', 'demo-010', 'DEMO'),
  ('11111111-0000-0000-0000-000000000011', 'Polyclinique Riviera [DÉMO]',     '+2250100000011', ST_SetSRID(ST_MakePoint(-3.9600, 5.3700), 4326)::geography, 'demo', 'demo-011', 'DEMO'),
  ('11111111-0000-0000-0000-000000000012', 'Centre de Santé d''Attécoubé [DÉMO]', '+2250100000012', ST_SetSRID(ST_MakePoint(-4.0480, 5.3400), 4326)::geography, 'demo', 'demo-012', 'DEMO'),
  ('11111111-0000-0000-0000-000000000013', 'Clinique du Banco [DÉMO]',        '+2250100000013', ST_SetSRID(ST_MakePoint(-4.0700, 5.3800), 4326)::geography, 'demo', 'demo-013', 'DEMO'),
  ('11111111-0000-0000-0000-000000000014', 'Hôpital d''Anyama [DÉMO]',        '+2250100000014', ST_SetSRID(ST_MakePoint(-4.0510, 5.4940), 4326)::geography, 'demo', 'demo-014', 'DEMO'),
  ('11111111-0000-0000-0000-000000000015', 'Centre Mère-Enfant de Songon [DÉMO]', '+2250100000015', ST_SetSRID(ST_MakePoint(-4.2530, 5.3080), 4326)::geography, 'demo', 'demo-015', 'DEMO');

-- Offre de services par établissement. Les gros CHU sont polyvalents (large
-- éventail de spécialités), les cliniques et centres de proximité plus ciblés,
-- à l'image du paysage hospitalier réel du Grand Abidjan. Chaque catégorie
-- « recherchable » de la taxonomie d'urgence (#91) doit apparier au moins un
-- établissement, sinon un symptôme ne renvoie JAMAIS de centre (issue #106) :
-- l'invariant est verrouillé par EmergencyTaxonomyFacilityCoverageIntegrationTest.
INSERT INTO facility_service (facility_id, service_code) VALUES
  -- CHU de Cocody — établissement de référence, polyvalent (plateau complet).
  ('11111111-0000-0000-0000-000000000001', 'emergency'),
  ('11111111-0000-0000-0000-000000000001', 'maternity'),
  ('11111111-0000-0000-0000-000000000001', 'surgery'),
  ('11111111-0000-0000-0000-000000000001', 'cardiology'),
  ('11111111-0000-0000-0000-000000000001', 'intensive_care'),
  ('11111111-0000-0000-0000-000000000001', 'icu'),
  ('11111111-0000-0000-0000-000000000001', 'pulmonology'),
  ('11111111-0000-0000-0000-000000000001', 'neurology'),
  ('11111111-0000-0000-0000-000000000001', 'neurosurgery'),
  ('11111111-0000-0000-0000-000000000001', 'general_surgery'),
  ('11111111-0000-0000-0000-000000000001', 'operating_room'),
  ('11111111-0000-0000-0000-000000000001', 'ortho_trauma'),
  ('11111111-0000-0000-0000-000000000001', 'burn_center'),
  ('11111111-0000-0000-0000-000000000001', 'toxicology'),
  ('11111111-0000-0000-0000-000000000001', 'infectiology'),
  ('11111111-0000-0000-0000-000000000001', 'ophthalmology'),
  ('11111111-0000-0000-0000-000000000001', 'ent'),
  ('11111111-0000-0000-0000-000000000001', 'gynecology'),
  ('11111111-0000-0000-0000-000000000001', 'obstetrics'),
  ('11111111-0000-0000-0000-000000000001', 'pediatrics'),
  ('11111111-0000-0000-0000-000000000001', 'pediatric_icu'),
  ('11111111-0000-0000-0000-000000000001', 'psychiatry'),
  ('11111111-0000-0000-0000-000000000001', 'psychiatric_emergency'),
  -- CHU de Treichville — polyvalent ; référence des maladies infectieuses (SMIT).
  ('11111111-0000-0000-0000-000000000002', 'emergency'),
  ('11111111-0000-0000-0000-000000000002', 'trauma'),
  ('11111111-0000-0000-0000-000000000002', 'surgery'),
  ('11111111-0000-0000-0000-000000000002', 'general_surgery'),
  ('11111111-0000-0000-0000-000000000002', 'operating_room'),
  ('11111111-0000-0000-0000-000000000002', 'ortho_trauma'),
  ('11111111-0000-0000-0000-000000000002', 'intensive_care'),
  ('11111111-0000-0000-0000-000000000002', 'icu'),
  ('11111111-0000-0000-0000-000000000002', 'neurology'),
  ('11111111-0000-0000-0000-000000000002', 'pulmonology'),
  ('11111111-0000-0000-0000-000000000002', 'infectiology'),
  ('11111111-0000-0000-0000-000000000002', 'toxicology'),
  ('11111111-0000-0000-0000-000000000002', 'ent'),
  ('11111111-0000-0000-0000-000000000002', 'ophthalmology'),
  -- CHU de Yopougon — polyvalent ; pôle mère-enfant et psychiatrie.
  ('11111111-0000-0000-0000-000000000003', 'emergency'),
  ('11111111-0000-0000-0000-000000000003', 'pediatrics'),
  ('11111111-0000-0000-0000-000000000003', 'maternity'),
  ('11111111-0000-0000-0000-000000000003', 'gynecology'),
  ('11111111-0000-0000-0000-000000000003', 'obstetrics'),
  ('11111111-0000-0000-0000-000000000003', 'pediatric_icu'),
  ('11111111-0000-0000-0000-000000000003', 'general_surgery'),
  ('11111111-0000-0000-0000-000000000003', 'operating_room'),
  ('11111111-0000-0000-0000-000000000003', 'intensive_care'),
  ('11111111-0000-0000-0000-000000000003', 'neurology'),
  ('11111111-0000-0000-0000-000000000003', 'neurosurgery'),
  ('11111111-0000-0000-0000-000000000003', 'psychiatry'),
  -- Hôpital Général d'Abobo — hôpital de zone, offre intermédiaire.
  ('11111111-0000-0000-0000-000000000004', 'emergency'),
  ('11111111-0000-0000-0000-000000000004', 'maternity'),
  ('11111111-0000-0000-0000-000000000004', 'pediatrics'),
  ('11111111-0000-0000-0000-000000000004', 'gynecology'),
  ('11111111-0000-0000-0000-000000000004', 'general_surgery'),
  -- Hôpital de Marcory — proximité, quelques spécialités.
  ('11111111-0000-0000-0000-000000000005', 'pediatrics'),
  ('11111111-0000-0000-0000-000000000005', 'emergency'),
  ('11111111-0000-0000-0000-000000000005', 'general_surgery'),
  ('11111111-0000-0000-0000-000000000005', 'ophthalmology'),
  -- PISAM (Plateau) — clinique privée haut de gamme, plateau technique large.
  ('11111111-0000-0000-0000-000000000006', 'cardiology'),
  ('11111111-0000-0000-0000-000000000006', 'surgery'),
  ('11111111-0000-0000-0000-000000000006', 'emergency'),
  ('11111111-0000-0000-0000-000000000006', 'general_surgery'),
  ('11111111-0000-0000-0000-000000000006', 'operating_room'),
  ('11111111-0000-0000-0000-000000000006', 'intensive_care'),
  ('11111111-0000-0000-0000-000000000006', 'icu'),
  ('11111111-0000-0000-0000-000000000006', 'pulmonology'),
  ('11111111-0000-0000-0000-000000000006', 'neurology'),
  ('11111111-0000-0000-0000-000000000006', 'ophthalmology'),
  ('11111111-0000-0000-0000-000000000006', 'ent'),
  -- Clinique d'Adjamé — orientée mère-enfant.
  ('11111111-0000-0000-0000-000000000007', 'maternity'),
  ('11111111-0000-0000-0000-000000000007', 'gynecology'),
  ('11111111-0000-0000-0000-000000000007', 'obstetrics'),
  ('11111111-0000-0000-0000-000000000007', 'pediatrics'),
  -- Hôpital de Koumassi — traumatologie et chirurgie.
  ('11111111-0000-0000-0000-000000000008', 'emergency'),
  ('11111111-0000-0000-0000-000000000008', 'trauma'),
  ('11111111-0000-0000-0000-000000000008', 'ortho_trauma'),
  ('11111111-0000-0000-0000-000000000008', 'general_surgery'),
  -- Centre de Port-Bouët — proximité.
  ('11111111-0000-0000-0000-000000000009', 'emergency'),
  ('11111111-0000-0000-0000-000000000009', 'pediatrics'),
  -- Clinique de Bingerville — mère-enfant + ophtalmologie.
  ('11111111-0000-0000-0000-000000000010', 'maternity'),
  ('11111111-0000-0000-0000-000000000010', 'pediatrics'),
  ('11111111-0000-0000-0000-000000000010', 'gynecology'),
  ('11111111-0000-0000-0000-000000000010', 'ophthalmology'),
  -- Polyclinique Riviera — privée, large éventail (dont santé mentale).
  ('11111111-0000-0000-0000-000000000011', 'surgery'),
  ('11111111-0000-0000-0000-000000000011', 'cardiology'),
  ('11111111-0000-0000-0000-000000000011', 'general_surgery'),
  ('11111111-0000-0000-0000-000000000011', 'operating_room'),
  ('11111111-0000-0000-0000-000000000011', 'intensive_care'),
  ('11111111-0000-0000-0000-000000000011', 'icu'),
  ('11111111-0000-0000-0000-000000000011', 'neurology'),
  ('11111111-0000-0000-0000-000000000011', 'infectiology'),
  ('11111111-0000-0000-0000-000000000011', 'ophthalmology'),
  ('11111111-0000-0000-0000-000000000011', 'ent'),
  ('11111111-0000-0000-0000-000000000011', 'psychiatry'),
  ('11111111-0000-0000-0000-000000000011', 'psychiatric_emergency'),
  -- Centre de Santé d'Attécoubé — proximité.
  ('11111111-0000-0000-0000-000000000012', 'emergency'),
  ('11111111-0000-0000-0000-000000000012', 'pediatrics'),
  -- Clinique du Banco — chirurgie de proximité.
  ('11111111-0000-0000-0000-000000000013', 'pediatrics'),
  ('11111111-0000-0000-0000-000000000013', 'general_surgery'),
  -- Hôpital d'Anyama — hôpital de zone.
  ('11111111-0000-0000-0000-000000000014', 'emergency'),
  ('11111111-0000-0000-0000-000000000014', 'maternity'),
  ('11111111-0000-0000-0000-000000000014', 'pediatrics'),
  ('11111111-0000-0000-0000-000000000014', 'general_surgery'),
  -- Centre Mère-Enfant de Songon — pôle mère-enfant.
  ('11111111-0000-0000-0000-000000000015', 'maternity'),
  ('11111111-0000-0000-0000-000000000015', 'pediatrics'),
  ('11111111-0000-0000-0000-000000000015', 'gynecology'),
  ('11111111-0000-0000-0000-000000000015', 'obstetrics'),
  ('11111111-0000-0000-0000-000000000015', 'pediatric_icu');

-- SÉCURITÉ (issue #124) : le jeton opérateur de démonstration n'est PLUS
-- versionné ici. Un secret en clair dans le seed était embarqué dans l'artefact
-- (JAR) et actif partout où ce jeu est chargé (profil local, y compris la démo
-- déployée), autorisant l'écriture de disponibilité sur TOUT établissement.
-- Le jeton de démo est désormais provisionné HORS du dépôt par les scripts
-- locaux (scripts/local-up.sh & e2e-smoke.sh, variable PORTAL_TOKEN) — jamais
-- présent en production. Ce DELETE purge l'ancien jeton résiduel au rechargement.
DELETE FROM portal_credential WHERE id = '22222222-0000-0000-0000-000000000001';
