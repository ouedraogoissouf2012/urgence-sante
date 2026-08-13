package com.urgencesante.facility.internal.adapter.out.directory;

import com.urgencesante.facility.internal.application.port.out.FacilityDirectoryPort;
import com.urgencesante.facility.internal.domain.directory.DataStatus;
import com.urgencesante.facility.internal.domain.directory.FacilityImportRecord;
import java.util.Objects;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Écriture idempotente de l'annuaire. L'upsert s'appuie sur l'index unique
 * naturel (source, external_ref) : rejouer le même lot met à jour sans créer de
 * doublon. La position est reconstruite en geography WGS84.
 */
@Component
class FacilityDirectoryAdapter implements FacilityDirectoryPort {

    private final JdbcTemplate jdbc;

    FacilityDirectoryAdapter(JdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    @Override
    public boolean existsByNaturalKey(String source, String externalRef) {
        final Boolean exists = jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM facility WHERE source = ? AND external_ref = ?)",
                Boolean.class, source, externalRef);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    @Transactional
    public void upsert(FacilityImportRecord record) {
        final UUID id = jdbc.queryForObject(
                "INSERT INTO facility "
                        + "(id, name, phone, location, source, external_ref, verified_at, steward, data_status) "
                        + "VALUES (?, ?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography, ?, ?, ?, ?, ?) "
                        + "ON CONFLICT (source, external_ref) "
                        + "WHERE source IS NOT NULL AND external_ref IS NOT NULL "
                        + "DO UPDATE SET name = EXCLUDED.name, phone = EXCLUDED.phone, "
                        + "location = EXCLUDED.location, verified_at = EXCLUDED.verified_at, "
                        + "steward = EXCLUDED.steward, data_status = EXCLUDED.data_status "
                        + "RETURNING id",
                UUID.class,
                UUID.randomUUID(), record.name(), record.phone(),
                record.longitude(), record.latitude(),
                record.source(), record.externalRef(),
                record.verifiedAt(), record.steward(), record.dataStatus().name());

        // Remplace l'ensemble des services (idempotent).
        jdbc.update("DELETE FROM facility_service WHERE facility_id = ?", id);
        for (final String service : record.serviceList()) {
            jdbc.update(
                    "INSERT INTO facility_service (facility_id, service_code) VALUES (?, ?)",
                    id, service);
        }
    }

    @Override
    public boolean hasDemoData() {
        // Le libellé du statut vient de l'enum (DataStatus.DEMO), pas d'un
        // littéral : renommer/retirer la valeur casserait la compilation ici au
        // lieu de laisser une garde de sécurité silencieusement désynchronisée.
        final Boolean exists = jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM facility WHERE data_status = ?)",
                Boolean.class, DataStatus.DEMO.name());
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public boolean hasNonDemoData() {
        final Boolean exists = jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM facility WHERE data_status <> ?)",
                Boolean.class, DataStatus.DEMO.name());
        return Boolean.TRUE.equals(exists);
    }

    @Override
    @Transactional
    public void purgeDemoData() {
        // facility_service (ON DELETE CASCADE) puis availability (ON DELETE
        // CASCADE via facility_service) sont purgés en cascade par la base
        // (V2__create_facility.sql, V5__referential_integrity.sql).
        // availability_history est un journal d'audit volontairement sans FK
        // (V5) : il survit à la purge, comme à toute suppression d'établissement.
        // portal_credential.facility_id n'a PAS de FK non plus (V7) : un
        // établissement démo purgé laisserait un jeton FACILITY_OPERATOR orphelin
        // (facility_id pointant vers un id disparu — échoue fermé, sans risque de
        // sécurité, mais incohérence référentielle). Non traité ici : le seed démo
        // livré n'insère JAMAIS de portal_credential (DemoSeedSecurityTest), donc
        // le cas ne peut survenir qu'un opérateur ait créé un jeton MANUELLEMENT
        // pour un établissement démo — hors périmètre de cette purge automatique.
        jdbc.update("DELETE FROM facility WHERE data_status = ?", DataStatus.DEMO.name());
    }
}
