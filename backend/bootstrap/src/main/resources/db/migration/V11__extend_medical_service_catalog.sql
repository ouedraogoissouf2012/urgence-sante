-- Enrichit le catalogue des services médicaux avec les spécialités « recherchées »
-- décrites dans la taxonomie d'urgence du client (document « Pour les urgences
-- médicales »). Fondation de l'orientation par symptômes (épopée #91, issue #92).
--
-- N'insère que les NOUVEAUX codes : les 6 services de V3 (emergency, maternity,
-- pediatrics, surgery, cardiology, trauma) ne sont pas redéclarés.
-- ON CONFLICT DO NOTHING : migration ré-applicable et robuste à un éventuel
-- recouvrement futur (principe ouvert/fermé — on étend par données, pas par code).

INSERT INTO medical_service (code, label, category) VALUES
    ('intensive_care',        'Réanimation',                'critical'),
    ('icu',                   'Unité de soins intensifs',   'critical'),
    ('burn_center',           'Centre de brûlés',           'critical'),
    ('pulmonology',           'Pneumologie',                'specialty'),
    ('neurology',             'Neurologie',                 'specialty'),
    ('neurosurgery',          'Neurochirurgie',             'surgery'),
    ('ortho_trauma',          'Traumato-orthopédie',        'surgery'),
    ('general_surgery',       'Chirurgie générale',         'surgery'),
    ('operating_room',        'Bloc opératoire',            'surgery'),
    ('gynecology',            'Gynécologie',                'maternal'),
    ('obstetrics',            'Obstétrique',                'maternal'),
    ('pediatric_icu',         'Réanimation pédiatrique',    'child'),
    ('toxicology',            'Toxicologie',                'specialty'),
    ('infectiology',          'Infectiologie',              'specialty'),
    ('ophthalmology',         'Ophtalmologie',              'specialty'),
    ('ent',                   'ORL',                        'specialty'),
    ('psychiatry',            'Psychiatrie',                'mental'),
    ('psychiatric_emergency', 'Urgences psychiatriques',    'mental')
ON CONFLICT (code) DO NOTHING;
