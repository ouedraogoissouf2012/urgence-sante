-- ============================================================================
-- DONNÉES SIMULÉES — DÉMONSTRATION UNIQUEMENT
-- Les établissements, coordonnées, téléphones et services de ce fichier sont
-- fictifs ou approximatifs. Ils ne doivent JAMAIS servir en production ni
-- être présentés comme des informations médicales réelles.
-- Rechargement idempotent : le jeu est vidé puis réinséré.
-- ============================================================================

TRUNCATE facility_service, availability, availability_history, facility;

INSERT INTO facility (id, name, phone, location) VALUES
  ('11111111-0000-0000-0000-000000000001', 'CHU de Cocody [DÉMO]',            '+2250100000001', ST_SetSRID(ST_MakePoint(-3.9851, 5.3496), 4326)::geography),
  ('11111111-0000-0000-0000-000000000002', 'CHU de Treichville [DÉMO]',       '+2250100000002', ST_SetSRID(ST_MakePoint(-4.0104, 5.2930), 4326)::geography),
  ('11111111-0000-0000-0000-000000000003', 'CHU de Yopougon [DÉMO]',          '+2250100000003', ST_SetSRID(ST_MakePoint(-4.0894, 5.3363), 4326)::geography),
  ('11111111-0000-0000-0000-000000000004', 'Hôpital Général d''Abobo [DÉMO]', '+2250100000004', ST_SetSRID(ST_MakePoint(-4.0210, 5.4322), 4326)::geography),
  ('11111111-0000-0000-0000-000000000005', 'Hôpital de Marcory [DÉMO]',       '+2250100000005', ST_SetSRID(ST_MakePoint(-3.9820, 5.3010), 4326)::geography),
  ('11111111-0000-0000-0000-000000000006', 'PISAM Plateau [DÉMO]',            '+2250100000006', ST_SetSRID(ST_MakePoint(-4.0190, 5.3330), 4326)::geography),
  ('11111111-0000-0000-0000-000000000007', 'Clinique d''Adjamé [DÉMO]',       '+2250100000007', ST_SetSRID(ST_MakePoint(-4.0230, 5.3660), 4326)::geography),
  ('11111111-0000-0000-0000-000000000008', 'Hôpital de Koumassi [DÉMO]',      '+2250100000008', ST_SetSRID(ST_MakePoint(-3.9530, 5.2890), 4326)::geography),
  ('11111111-0000-0000-0000-000000000009', 'Centre de Port-Bouët [DÉMO]',     '+2250100000009', ST_SetSRID(ST_MakePoint(-3.9280, 5.2570), 4326)::geography),
  ('11111111-0000-0000-0000-000000000010', 'Clinique de Bingerville [DÉMO]',  '+2250100000010', ST_SetSRID(ST_MakePoint(-3.8850, 5.3560), 4326)::geography),
  ('11111111-0000-0000-0000-000000000011', 'Polyclinique Riviera [DÉMO]',     '+2250100000011', ST_SetSRID(ST_MakePoint(-3.9600, 5.3700), 4326)::geography),
  ('11111111-0000-0000-0000-000000000012', 'Centre de Santé d''Attécoubé [DÉMO]', '+2250100000012', ST_SetSRID(ST_MakePoint(-4.0480, 5.3400), 4326)::geography),
  ('11111111-0000-0000-0000-000000000013', 'Clinique du Banco [DÉMO]',        '+2250100000013', ST_SetSRID(ST_MakePoint(-4.0700, 5.3800), 4326)::geography),
  ('11111111-0000-0000-0000-000000000014', 'Hôpital d''Anyama [DÉMO]',        '+2250100000014', ST_SetSRID(ST_MakePoint(-4.0510, 5.4940), 4326)::geography),
  ('11111111-0000-0000-0000-000000000015', 'Centre Mère-Enfant de Songon [DÉMO]', '+2250100000015', ST_SetSRID(ST_MakePoint(-4.2530, 5.3080), 4326)::geography);

INSERT INTO facility_service (facility_id, service_code) VALUES
  ('11111111-0000-0000-0000-000000000001', 'emergency'),
  ('11111111-0000-0000-0000-000000000001', 'maternity'),
  ('11111111-0000-0000-0000-000000000001', 'surgery'),
  ('11111111-0000-0000-0000-000000000001', 'cardiology'),
  ('11111111-0000-0000-0000-000000000002', 'emergency'),
  ('11111111-0000-0000-0000-000000000002', 'trauma'),
  ('11111111-0000-0000-0000-000000000002', 'surgery'),
  ('11111111-0000-0000-0000-000000000003', 'emergency'),
  ('11111111-0000-0000-0000-000000000003', 'pediatrics'),
  ('11111111-0000-0000-0000-000000000003', 'maternity'),
  ('11111111-0000-0000-0000-000000000004', 'emergency'),
  ('11111111-0000-0000-0000-000000000004', 'maternity'),
  ('11111111-0000-0000-0000-000000000005', 'pediatrics'),
  ('11111111-0000-0000-0000-000000000005', 'emergency'),
  ('11111111-0000-0000-0000-000000000006', 'cardiology'),
  ('11111111-0000-0000-0000-000000000006', 'surgery'),
  ('11111111-0000-0000-0000-000000000006', 'emergency'),
  ('11111111-0000-0000-0000-000000000007', 'maternity'),
  ('11111111-0000-0000-0000-000000000008', 'emergency'),
  ('11111111-0000-0000-0000-000000000008', 'trauma'),
  ('11111111-0000-0000-0000-000000000009', 'emergency'),
  ('11111111-0000-0000-0000-000000000010', 'maternity'),
  ('11111111-0000-0000-0000-000000000010', 'pediatrics'),
  ('11111111-0000-0000-0000-000000000011', 'surgery'),
  ('11111111-0000-0000-0000-000000000011', 'cardiology'),
  ('11111111-0000-0000-0000-000000000012', 'emergency'),
  ('11111111-0000-0000-0000-000000000013', 'pediatrics'),
  ('11111111-0000-0000-0000-000000000014', 'emergency'),
  ('11111111-0000-0000-0000-000000000014', 'maternity'),
  ('11111111-0000-0000-0000-000000000015', 'maternity'),
  ('11111111-0000-0000-0000-000000000015', 'pediatrics');
