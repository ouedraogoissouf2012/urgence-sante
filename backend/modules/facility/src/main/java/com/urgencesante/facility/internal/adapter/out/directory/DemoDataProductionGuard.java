package com.urgencesante.facility.internal.adapter.out.directory;

import com.urgencesante.facility.internal.application.port.out.FacilityDirectoryPort;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Garde de production : au démarrage en profil « production », échoue si des
 * données de démonstration (fictives) sont présentes dans l'annuaire. Les
 * données démo NE PEUVENT PAS être servies en production (issue #41).
 *
 * <p>{@link Order} (explicite, bien qu'égal au défaut) : documente que cette
 * garde s'exécute APRÈS {@code FacilityImportRunner} — voir sa Javadoc —
 * plutôt que de dépendre d'un ordre d'enregistrement de bean non garanti.
 */
@Component
@Profile("production")
@Order(Ordered.LOWEST_PRECEDENCE)
class DemoDataProductionGuard implements ApplicationRunner {

    private final FacilityDirectoryPort directoryPort;

    DemoDataProductionGuard(FacilityDirectoryPort directoryPort) {
        this.directoryPort = directoryPort;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (directoryPort.hasDemoData()) {
            throw new IllegalStateException(
                    "Données de démonstration détectées en production : "
                            + "démarrage refusé. Purgez les établissements data_status = DEMO.");
        }
    }
}
