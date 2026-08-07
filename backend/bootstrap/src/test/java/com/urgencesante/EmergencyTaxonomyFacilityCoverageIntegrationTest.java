package com.urgencesante;

import static org.assertj.core.api.Assertions.assertThat;

import com.urgencesante.emergencytaxonomy.EmergencyTaxonomyFacade;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
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
 * établissement du jeu de démonstration livré. Sans cela, l'utilisateur choisit
 * un symptôme et n'obtient JAMAIS de centre — le parcours principal est cassé
 * pour toute cette catégorie (issue #106).
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
            Pattern.compile("'[0-9a-f-]{36}',\\s*'([a-z_]+)'");

    @Autowired
    private EmergencyTaxonomyFacade taxonomy;

    @Test
    void chaque_categorie_recherchable_est_couverte_par_au_moins_un_etablissement() throws IOException {
        final Set<String> offered = servicesOffertsParLeJeuDeDemo();

        final List<String> categoriesSansCentre = taxonomy.categories().stream()
                .filter(category -> !category.directCallOnly())
                .filter(category -> category.serviceCodes().stream().noneMatch(offered::contains))
                .map(category -> category.id() + " " + category.serviceCodes())
                .toList();

        assertThat(categoriesSansCentre)
                .as("catégories recherchables dont AUCUN code de service n'est offert par un "
                        + "établissement du jeu de démonstration (codes offerts = %s)", offered)
                .isEmpty();
    }

    /**
     * Ensemble des codes de services réellement offerts par le jeu de démo,
     * extraits des lignes {@code facility_service}. La contrainte de format
     * (UUID de 36 caractères suivi du code) exclut les noms d'établissements et
     * les commentaires — seules les vraies paires offre sont captées.
     */
    private static Set<String> servicesOffertsParLeJeuDeDemo() throws IOException {
        final String seed = StreamUtils.copyToString(
                new ClassPathResource("db/local-seed/R__seed_demo.sql").getInputStream(),
                StandardCharsets.UTF_8);
        final Set<String> codes = new LinkedHashSet<>();
        final Matcher matcher = OFFERED_SERVICE.matcher(seed);
        while (matcher.find()) {
            codes.add(matcher.group(1));
        }
        assertThat(codes)
                .as("le jeu de démonstration doit déclarer des offres de services")
                .isNotEmpty();
        return codes;
    }
}
