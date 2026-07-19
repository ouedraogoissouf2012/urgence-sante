package com.urgencesante.facility.internal.adapter.out.directory;

import com.urgencesante.facility.internal.application.port.out.FacilityDirectoryPort;
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
        final Boolean exists = jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM facility WHERE data_status = 'DEMO')", Boolean.class);
        return Boolean.TRUE.equals(exists);
    }
}
