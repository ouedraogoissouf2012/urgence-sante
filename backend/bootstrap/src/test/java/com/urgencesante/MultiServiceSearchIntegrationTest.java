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
 * Recherche des candidats d'orientation sur PostgreSQL réel. Un symptôme
 * couvrant plusieurs spécialités trouve les centres offrant AU MOINS UN des
 * services (sémantique OU, #94), triés par proximité — un centre hors de
 * l'ensemble est exclu même s'il est plus proche. Seuls les centres accueillant
 * les urgences (2e et 3e niveau) sont candidats : un centre de 1er niveau n'est
 * jamais proposé, même le plus proche (demande client, ADR-008).
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
        return record(ref, name, lat, service, "TERTIARY");
    }

    private static FacilityImportRecord record(
            String ref, String name, double lat, String service, String careLevel) {
        return new FacilityImportRecord(
                "ms-src", ref, name, "+2252722481000", lat, -4.00, Set.of(service), careLevel,
                DataStatus.VERIFIED, LocalDate.of(2026, 1, 1), "cellule");
    }

    @Test
    void trouve_les_centres_offrant_l_un_des_services_demandes_par_proximite() {
        importFacilities.importDirectory(List.of(
                record("ms-1", "Pneumo", 5.360, "pulmonology"),
                record("ms-2", "Reanim", 5.370, "intensive_care"),
                // Le plus proche du point de recherche, mais hors de l'ensemble demandé.
                record("ms-3", "Cardio", 5.351, "cardiology")));

        final List<FacilityView> result = facilityFacade.findEmergencyCapableNearby(
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
                Set.of("cardiology", "pulmonology", "intensive_care"), "TERTIARY",
                DataStatus.VERIFIED, LocalDate.of(2026, 1, 1), "cellule")));

        final List<FacilityView> result = facilityFacade.findEmergencyCapableNearby(
                Set.of("cardiology", "pulmonology"), 5.35, -4.00, 50_000, 10);

        assertThat(result)
                .extracting(FacilityView::name)
                .containsExactly("Polyvalent");
    }

    // « obstetrics » : aucun autre test de la suite (base partagée) n'insère de
    // centre offrant ce service — « maternity » est déjà posé à ce point par
    // Audit/Outbox/PortalSecurity et fausserait l'ordre attendu.
    @Test
    void un_centre_de_premier_niveau_n_est_jamais_propose_meme_le_plus_proche() {
        importFacilities.importDirectory(List.of(
                // Le plus proche et offrant le service, mais de 1er niveau.
                record("ms-p", "Centre de sante", 5.351, "obstetrics", "PRIMARY"),
                record("ms-s", "Hopital general", 5.360, "obstetrics", "SECONDARY"),
                record("ms-t", "CHU", 5.370, "obstetrics", "TERTIARY")));

        final List<FacilityView> result = facilityFacade.findEmergencyCapableNearby(
                Set.of("obstetrics"), 5.35, -4.00, 50_000, 10);

        assertThat(result)
                .extracting(FacilityView::name)
                .containsExactly("Hopital general", "CHU");
    }

    @Test
    void le_filtre_s_applique_avant_la_limite_un_centre_de_premier_niveau_ne_prend_aucune_place() {
        // Le vivier d'orientation est borné (OrientationService) : si le 1er
        // niveau était écarté APRÈS la limite, des centres de santé proches
        // pourraient évincer tous les hôpitaux du résultat.
        importFacilities.importDirectory(List.of(
                record("ms-p1", "Centre de sante 1", 5.351, "obstetrics", "PRIMARY"),
                record("ms-p2", "Centre de sante 2", 5.352, "obstetrics", "PRIMARY"),
                record("ms-s", "Hopital general", 5.380, "obstetrics", "SECONDARY")));

        final List<FacilityView> result = facilityFacade.findEmergencyCapableNearby(
                Set.of("obstetrics"), 5.35, -4.00, 50_000, 1);

        assertThat(result)
                .extracting(FacilityView::name)
                .containsExactly("Hopital general");
    }
}
