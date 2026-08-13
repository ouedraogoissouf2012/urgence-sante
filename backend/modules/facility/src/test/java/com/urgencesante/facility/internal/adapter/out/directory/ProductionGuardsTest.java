package com.urgencesante.facility.internal.adapter.out.directory;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import com.urgencesante.facility.internal.application.port.out.FacilityDirectoryPort;
import com.urgencesante.facility.internal.domain.directory.FacilityImportRecord;
import org.junit.jupiter.api.Test;

/**
 * Gardes de démarrage en profil production (issue #123) : aucune des deux ne
 * touche de base réelle (port {@link FacilityDirectoryPort} en mémoire), donc
 * pas de dépendance à PostGIS — contrairement à {@code FacilityImportIntegrationTest}
 * qui vérifie le SQL réel de {@code hasDemoData}/{@code hasNonDemoData}.
 */
class ProductionGuardsTest {

    /** Faux annuaire en mémoire, état fixé directement (pas d'upsert à rejouer ici). */
    private static final class FakeDirectory implements FacilityDirectoryPort {
        private boolean demoData;
        private boolean nonDemoData;

        @Override
        public boolean existsByNaturalKey(String source, String externalRef) {
            throw new UnsupportedOperationException("non exercé par les gardes");
        }

        @Override
        public void upsert(FacilityImportRecord record) {
            throw new UnsupportedOperationException("non exercé par les gardes");
        }

        @Override
        public boolean hasDemoData() {
            return demoData;
        }

        @Override
        public boolean hasNonDemoData() {
            return nonDemoData;
        }

        @Override
        public void purgeDemoData() {
            throw new UnsupportedOperationException("non exercé par les gardes");
        }
    }

    @Test
    void demoDataProductionGuard_refuse_le_demarrage_si_donnees_demo_presentes() {
        final FakeDirectory directory = new FakeDirectory();
        directory.demoData = true;

        assertThatIllegalStateException()
                .isThrownBy(() -> new DemoDataProductionGuard(directory).run(null))
                .withMessageContaining("Données de démonstration détectées");
    }

    @Test
    void demoDataProductionGuard_demarre_sans_donnees_demo() {
        final FakeDirectory directory = new FakeDirectory();
        directory.demoData = false;

        assertThatCode(() -> new DemoDataProductionGuard(directory).run(null))
                .doesNotThrowAnyException();
    }

    @Test
    void emptyDirectoryProductionGuard_refuse_le_demarrage_si_annuaire_vide() {
        final FakeDirectory directory = new FakeDirectory();
        directory.nonDemoData = false;

        assertThatIllegalStateException()
                .isThrownBy(() -> new EmptyDirectoryProductionGuard(directory).run(null))
                .withMessageContaining("Aucun établissement");
    }

    @Test
    void emptyDirectoryProductionGuard_demarre_avec_au_moins_un_etablissement_reel() {
        final FakeDirectory directory = new FakeDirectory();
        directory.nonDemoData = true;

        assertThatCode(() -> new EmptyDirectoryProductionGuard(directory).run(null))
                .doesNotThrowAnyException();
    }
}
