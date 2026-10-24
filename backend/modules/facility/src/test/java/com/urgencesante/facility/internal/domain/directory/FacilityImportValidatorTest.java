package com.urgencesante.facility.internal.domain.directory;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FacilityImportValidatorTest {

    private final FacilityImportValidator validator =
            new FacilityImportValidator(code -> Set.of("emergency", "maternity").contains(code));

    private FacilityImportRecord record(
            double lat, double lon, String phone, Set<String> services, DataStatus status,
            LocalDate verifiedAt) {
        return withCareLevel("TERTIARY", lat, lon, phone, services, status, verifiedAt);
    }

    private FacilityImportRecord withCareLevel(
            String careLevel, double lat, double lon, String phone, Set<String> services,
            DataStatus status, LocalDate verifiedAt) {
        return new FacilityImportRecord(
                "src", "ref-1", "Hôpital Test", phone, lat, lon, services, careLevel,
                status, verifiedAt, "resp");
    }

    @Test
    void accepte_un_enregistrement_conforme() {
        final var reasons = validator.validate(record(
                5.35, -4.0, "+2252722481000", Set.of("emergency"),
                DataStatus.VERIFIED, LocalDate.of(2026, 1, 1)));

        assertThat(reasons).isEmpty();
    }

    @Test
    void rejette_des_coordonnees_hors_abidjan() {
        final var reasons = validator.validate(record(
                6.80, -5.30, "+2252722481000", Set.of("emergency"),
                DataStatus.PROVISIONAL, null));

        assertThat(reasons).anyMatch(r -> r.contains("hors du Grand Abidjan"));
    }

    @Test
    void rejette_un_telephone_invalide() {
        final var reasons = validator.validate(record(
                5.35, -4.0, "12", Set.of("emergency"), DataStatus.PROVISIONAL, null));

        assertThat(reasons).anyMatch(r -> r.contains("téléphone invalide"));
    }

    @Test
    void rejette_un_service_inconnu() {
        final var reasons = validator.validate(record(
                5.35, -4.0, "+2252722481000", Set.of("radiologie-inexistante"),
                DataStatus.PROVISIONAL, null));

        assertThat(reasons).anyMatch(r -> r.contains("service inconnu"));
    }

    @Test
    void exige_une_date_de_verification_pour_le_statut_verified() {
        final var reasons = validator.validate(record(
                5.35, -4.0, "+2252722481000", Set.of("emergency"), DataStatus.VERIFIED, null));

        assertThat(reasons).anyMatch(r -> r.contains("date de vérification"));
    }

    @Test
    void exige_provenance_nom_et_services() {
        final var reasons = validator.validate(new FacilityImportRecord(
                null, null, "  ", "+2252722481000", 5.35, -4.0, Set.of(), "SECONDARY",
                DataStatus.PROVISIONAL, null, null));

        assertThat(reasons)
                .anyMatch(r -> r.contains("provenance"))
                .anyMatch(r -> r.contains("nom requis"))
                .anyMatch(r -> r.contains("au moins un service"));
    }

    @Test
    void exige_un_niveau_de_soins() {
        // Sans niveau, impossible de savoir si le centre accueille les urgences :
        // la ligne est rejetée, jamais affichée ni masquée en silence.
        final var reasons = validator.validate(withCareLevel(
                null, 5.35, -4.0, "+2252722481000", Set.of("emergency"),
                DataStatus.PROVISIONAL, null));

        assertThat(reasons).anyMatch(r -> r.contains("niveau de soins requis"));
    }

    @Test
    void rejette_un_niveau_de_soins_inconnu_en_le_citant() {
        final var reasons = validator.validate(withCareLevel(
                "QUATERNARY", 5.35, -4.0, "+2252722481000", Set.of("emergency"),
                DataStatus.PROVISIONAL, null));

        assertThat(reasons).anyMatch(r -> r.contains("niveau de soins inconnu : QUATERNARY"));
    }

    @Test
    void accepte_un_centre_de_premier_niveau() {
        // Le 1er niveau est une donnée valide de l'annuaire : il est importé,
        // c'est la recherche d'orientation qui l'écarte.
        final var reasons = validator.validate(withCareLevel(
                "PRIMARY", 5.35, -4.0, "+2252722481000", Set.of("maternity"),
                DataStatus.PROVISIONAL, null));

        assertThat(reasons).isEmpty();
    }
}
