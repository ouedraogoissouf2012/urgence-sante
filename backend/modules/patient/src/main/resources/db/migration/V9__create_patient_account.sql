-- Comptes patients. Identifiant : numéro de téléphone (normalisé, unique).
-- Le mot de passe n'est stocké que sous forme d'empreinte BCrypt (salée) :
-- le mot de passe en clair n'existe jamais côté serveur.
-- Aucune donnée médicale ici : les données de santé restent sur l'appareil.
CREATE TABLE patient_account (
    id             UUID PRIMARY KEY,
    phone          TEXT NOT NULL UNIQUE,
    password_hash  TEXT NOT NULL,
    active         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
