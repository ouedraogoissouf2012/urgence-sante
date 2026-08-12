-- Catalogue des services médicaux. Extensible par ajout de lignes, sans
-- modification du code (principe ouvert/fermé pour l'orientation).
CREATE TABLE medical_service (
    code     TEXT PRIMARY KEY,
    label    TEXT NOT NULL,
    category TEXT
);

CREATE INDEX idx_medical_service_category ON medical_service (category);

INSERT INTO medical_service (code, label, category) VALUES
    ('emergency',  'Urgences',       'emergency'),
    ('maternity',  'Maternité',      'maternal'),
    ('pediatrics', 'Pédiatrie',      'child'),
    ('surgery',    'Chirurgie',      'surgery'),
    ('cardiology', 'Cardiologie',    'specialty'),
    ('trauma',     'Traumatologie',  'emergency');
