package com.urgencesante;

import static org.assertj.core.api.Assertions.assertThat;

import com.urgencesante.facility.FacilityFacade;
import com.urgencesante.facility.FacilityView;
import com.urgencesante.facility.internal.application.port.in.ImportFacilitiesUseCase;
import com.urgencesante.facility.internal.domain.directory.DataStatus;
import com.urgencesante.facility.internal.domain.directory.FacilityImportRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Recherche multi-services sur PostgreSQL réel (#94). Un symptôme couvrant
 * plusieurs spécialités trouve les centres offrant AU MOINS UN des services
 * (sémantique OU), triés par proximité — un centre hors de l'ensemble est exclu
 * même s'il est plus proche.
 */
@SpringBootTest
@ActiveProfiles("test")
class MultiServiceSearchIntegrationTest extends AbstractPostgisIntegrationTest {

    @Autowired
    private ImportFacilitiesUseCase importFacilities;

    @Autowired
    private FacilityFacade facilityFacade;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void clean() {
        jdbc.update("DELETE FROM facility_service WHERE facility_id IN "
                + "(SELECT id FROM facility WHERE source = 'ms-src')");
        jdbc.update("DELETE FROM facility WHERE source = 'ms-src'");
    }

    private static FacilityImportRecord record(String ref, String name, double lat, String service) {
        return new FacilityImportRecord(
                "ms-src", ref, name, "+2252722481000", lat, -4.00, Set.of(service),
                DataStatus.VERIFIED, LocalDate.of(2026, 1, 1), "cellule");
    }

    @Test
    void trouve_les_centres_offrant_l_un_des_services_demandes_par_proximite() {
        importFacilities.importDirectory(List.of(
                record("ms-1", "Pneumo", 5.360, "pulmonology"),
                record("ms-2", "Reanim", 5.370, "intensive_care"),
                // Le plus proche du point de recherche, mais hors de l'ensemble demandé.
                record("ms-3", "Cardio", 5.351, "cardiology")));

        final List<FacilityView> result = facilityFacade.findNearbyOfferingAny(
                Set.of("pulmonology", "intensive_care"), 5.35, -4.00, 50_000, 10);

        assertThat(result)
                .extracting(FacilityView::name)
                .containsExactly("Pneumo", "Reanim");
    }

    @Test
    void un_etablissement_offrant_plusieurs_services_est_retourne_une_seule_fois() {
        // Garde-fou #105 : findAllById porte @EntityGraph("services") sur une
        // @ElementCollection → le JOIN FETCH renvoie plusieurs lignes pour un
        // établissement à plusieurs services. Vérifié empiriquement en CI :
        // Hibernate dédoublonne les racines (aucun 500). La fonction de fusion de
        // Collectors.toMap (FacilityPersistenceAdapter) rend cette garantie
        // indépendante de ce comportement implicite. Le centre apparaît UNE fois.
        importFacilities.importDirectory(List.of(new FacilityImportRecord(
                "ms-src", "ms-multi", "Polyvalent", "+2252722481000", 5.35, -4.00,
                Set.of("cardiology", "pulmonology", "intensive_care"),
                DataStatus.VERIFIED, LocalDate.of(2026, 1, 1), "cellule")));

        final List<FacilityView> result = facilityFacade.findNearbyOfferingAny(
                Set.of("cardiology", "pulmonology"), 5.35, -4.00, 50_000, 10);

        assertThat(result)
                .extracting(FacilityView::name)
                .containsExactly("Polyvalent");
    }
}
