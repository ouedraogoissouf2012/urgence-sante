package com.urgencesante;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Le catalogue des services médicaux (migrations V3 + V11) doit exposer toutes
 * les spécialités « recherchées » de la taxonomie d'urgence du client (document
 * « Pour les urgences médicales », épopée #91). Ce test verrouille ce contrat :
 * une migration future qui supprimerait l'une de ces spécialités échouerait ici.
 */
@SpringBootTest
@ActiveProfiles("test")
class MedicalServiceCatalogIntegrationTest extends AbstractPostgisIntegrationTest {

    /** Spécialités attendues : les 6 historiques (V3) + les 18 ajoutées (V11). */
    private static final List<String> EXPECTED_CODES = List.of(
            "emergency", "cardiology", "maternity", "pediatrics", "surgery", "trauma",
            "intensive_care", "icu", "burn_center", "pulmonology", "neurology",
            "neurosurgery", "ortho_trauma", "general_surgery", "operating_room",
            "gynecology", "obstetrics", "pediatric_icu", "toxicology", "infectiology",
            "ophthalmology", "ent", "psychiatry", "psychiatric_emergency");

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void le_catalogue_contient_toutes_les_specialites_de_la_taxonomie() {
        final List<String> codes =
                jdbc.queryForList("SELECT code FROM medical_service", String.class);

        assertThat(codes).containsAll(EXPECTED_CODES);
    }

    @Test
    void chaque_service_du_catalogue_a_un_libelle_non_vide() {
        final Integer sansLibelle = jdbc.queryForObject(
                "SELECT count(*) FROM medical_service WHERE label IS NULL OR btrim(label) = ''",
                Integer.class);

        assertThat(sansLibelle).isZero();
    }
}
