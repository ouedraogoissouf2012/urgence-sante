-- Table des établissements de santé. La localisation est un point géographique
-- (geography/WGS84) pour des distances en mètres et un index spatial GIST.
CREATE TABLE facility (
    id       UUID PRIMARY KEY,
    name     TEXT NOT NULL,
    phone    TEXT,
    location geography(Point, 4326) NOT NULL
);

CREATE INDEX idx_facility_location ON facility USING GIST (location);

-- Services médicaux offerts par établissement (relation 1-N).
CREATE TABLE facility_service (
    facility_id  UUID NOT NULL REFERENCES facility (id) ON DELETE CASCADE,
    service_code TEXT NOT NULL,
    PRIMARY KEY (facility_id, service_code)
);

CREATE INDEX idx_facility_service_code ON facility_service (service_code);
