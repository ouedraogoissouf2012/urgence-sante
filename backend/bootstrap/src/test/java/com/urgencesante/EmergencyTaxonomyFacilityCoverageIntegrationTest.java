package com.urgencesante;

import static org.assertj.core.api.Assertions.assertThat;

import com.urgencesante.emergencytaxonomy.EmergencyTaxonomyFacade;
import com.urgencesante.facility.internal.domain.model.CareLevel;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StreamUtils;

/**
 * Invariant de COUVERTURE offre ↔ demande : chaque catégorie d'urgence donnant
 * lieu à une recherche (hors « appel direct ») doit apparier au moins un
 * établissement du jeu de démonstration livré QUI ACCUEILLE LES URGENCES (2e ou
 * 3e niveau — un centre de 1er niveau n'est jamais proposé, ADR-008). Sans
 * cela, l'utilisateur choisit un symptôme et n'obtient JAMAIS de centre — le
 * parcours principal est cassé pour toute cette catégorie (issue #106).
 *
 * <p>Complémentaire de {@link EmergencyTaxonomyCatalogConsistencyIntegrationTest},
 * qui ne vérifie que taxonomie → catalogue : un code peut exister au catalogue
 * (V3/V11) tout en n'étant offert par AUCUN établissement. Ici on verrouille
 * l'axe manquant — les codes demandés sont RÉELLEMENT offerts par des centres.
 *
 * <p>La source de vérité de l'offre est le jeu livré lui-même
 * ({@code db/local-seed/R__seed_demo.sql}, copié depuis
 * {@code infrastructure/demo/}) : le test lit les codes qu'il déclare, jamais
 * une liste dupliquée qui pourrait diverger.
 */
@SpringBootTest
@ActiveProfiles("test")
class EmergencyTaxonomyFacilityCoverageIntegrationTest extends AbstractPostgisIntegrationTest {

    /** Une paire {@code ('<uuid>', '<code>')} d'une ligne facility_service du jeu. */
    private static final Pattern OFFERED_SERVICE =
            Pattern.compile("'([0-9a-f-]{36})',\\s*'([a-z_]+)'");

    /**
     * Une ligne facility du jeu : l'UUID en tête, le niveau de soins en DERNIÈRE
     * valeur (le {@code .*} gourmand saute nom, position, provenance et statut).
     */
    private static final Pattern FACILITY_CARE_LEVEL =
            Pattern.compile("\\('([0-9a-f-]{36})',.*'([A-Z]+)'\\)[,;]");

    @Autowired
    private EmergencyTaxonomyFacade taxonomy;

    @Test
    void chaque_categorie_recherchable_est_couverte_par_au_moins_un_etablissement() throws IOException {
        final Set<String> offered = servicesOffertsAuxUrgencesParLeJeuDeDemo();

        final List<String> categoriesSansCentre = taxonomy.categories().stream()
                .filter(category -> !category.directCallOnly())
                .filter(category -> category.serviceCodes().stream().noneMatch(offered::contains))
                .map(category -> category.id() + " " + category.serviceCodes())
                .toList();

        assertThat(categoriesSansCentre)
                .as("catégories recherchables dont AUCUN code de service n'est offert par un "
                        + "établissement du jeu de démonstration accueillant les urgences "
                        + "(codes offerts = %s)", offered)
                .isEmpty();
    }

    /**
     * Ensemble des codes de services offerts par les établissements du jeu de
     * démo qui ACCUEILLENT LES URGENCES, extraits des lignes {@code
     * facility_service}. La contrainte de format (UUID de 36 caractères suivi du
     * code) exclut les noms d'établissements et les commentaires — seules les
     * vraies paires offre sont captées. La règle d'éligibilité est celle du
     * domaine ({@link CareLevel#isEmergencyCapable()}), jamais une copie.
     */
    private static Set<String> servicesOffertsAuxUrgencesParLeJeuDeDemo() throws IOException {
        final String seed = StreamUtils.copyToString(
                new ClassPathResource("db/local-seed/R__seed_demo.sql").getInputStream(),
                StandardCharsets.UTF_8);
        final Map<String, CareLevel> levels = niveauxDeSoinsParEtablissement(seed);
        final Set<String> codes = new LinkedHashSet<>();
        final Matcher matcher = OFFERED_SERVICE.matcher(seed);
        while (matcher.find()) {
            final CareLevel level = levels.get(matcher.group(1));
            assertThat(level)
                    .as("l'établissement %s offre un service mais n'a pas de niveau de soins",
                            matcher.group(1))
                    .isNotNull();
            if (level.isEmergencyCapable()) {
                codes.add(matcher.group(2));
            }
        }
        assertThat(codes)
                .as("le jeu de démonstration doit déclarer des offres de services aux urgences")
                .isNotEmpty();
        return codes;
    }

    private static Map<String, CareLevel> niveauxDeSoinsParEtablissement(String seed) {
        final Map<String, CareLevel> levels = new HashMap<>();
        final Matcher matcher = FACILITY_CARE_LEVEL.matcher(seed);
        while (matcher.find()) {
            final Optional<CareLevel> level = CareLevel.fromCode(matcher.group(2));
            assertThat(level)
                    .as("niveau de soins inconnu « %s » pour %s", matcher.group(2), matcher.group(1))
                    .isPresent();
            levels.put(matcher.group(1), level.orElseThrow());
        }
        return levels;
    }
}
