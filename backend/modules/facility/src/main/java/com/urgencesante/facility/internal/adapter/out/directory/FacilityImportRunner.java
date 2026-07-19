package com.urgencesante.facility.internal.adapter.out.directory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urgencesante.facility.internal.application.port.in.ImportFacilitiesUseCase;
import com.urgencesante.facility.internal.domain.directory.DataStatus;
import com.urgencesante.facility.internal.domain.directory.FacilityImportRecord;
import com.urgencesante.facility.internal.domain.directory.ImportReport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Import d'annuaire au démarrage depuis un fichier JSON, activé UNIQUEMENT si la
 * propriété {@code facility.import.file} est fournie. Journalise un rapport
 * (insérés, mis à jour, rejetés avec motifs) pour revue manuelle.
 */
@Component
@ConditionalOnProperty(name = "facility.import.file")
class FacilityImportRunner implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(FacilityImportRunner.class);

    private final ImportFacilitiesUseCase importFacilities;
    private final ObjectMapper objectMapper;
    private final String filePath;

    FacilityImportRunner(
            ImportFacilitiesUseCase importFacilities,
            ObjectMapper objectMapper,
            org.springframework.core.env.Environment environment) {
        this.importFacilities = importFacilities;
        this.objectMapper = objectMapper;
        this.filePath = environment.getProperty("facility.import.file");
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        final byte[] content = Files.readAllBytes(Path.of(filePath));
        final List<Map<String, Object>> rows =
                objectMapper.readValue(content, new com.fasterxml.jackson.core.type.TypeReference<>() {});
        final List<FacilityImportRecord> records = new ArrayList<>();
        for (final Map<String, Object> row : rows) {
            records.add(toRecord(row));
        }
        final ImportReport report = importFacilities.importDirectory(records);
        LOG.info("Import annuaire depuis {} : {} insérés, {} mis à jour, {} rejetés",
                filePath, report.inserted(), report.updated(), report.rejectedCount());
        for (final ImportReport.Rejection rejection : report.rejected()) {
            LOG.warn("Rejeté [{}/{}] {} : {}",
                    rejection.source(), rejection.externalRef(), rejection.name(), rejection.reasons());
        }
    }

    @SuppressWarnings("unchecked")
    private static FacilityImportRecord toRecord(Map<String, Object> row) {
        final Object services = row.get("services");
        final LocalDate verifiedAt = row.get("verifiedAt") == null
                ? null
                : LocalDate.parse(row.get("verifiedAt").toString());
        return new FacilityImportRecord(
                str(row, "source"),
                str(row, "externalRef"),
                str(row, "name"),
                str(row, "phone"),
                ((Number) row.get("latitude")).doubleValue(),
                ((Number) row.get("longitude")).doubleValue(),
                services == null ? Set.of() : Set.copyOf((List<String>) services),
                DataStatus.valueOf(str(row, "dataStatus")),
                verifiedAt,
                str(row, "steward"));
    }

    private static String str(Map<String, Object> row, String key) {
        final Object value = row.get(key);
        return value == null ? null : value.toString();
    }
}
