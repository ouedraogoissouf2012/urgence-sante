package com.urgencesante.facility.internal.adapter.out.directory;

import com.urgencesante.facility.internal.application.port.out.FacilityDirectoryPort;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Garde de production : au démarrage en profil « production », échoue si
 * l'annuaire ne contient AUCUN établissement non-démo (issue #123).
 *
 * <p>Complète {@link DemoDataProductionGuard} plutôt que de vérifier l'inverse
 * de la même chose : {@code FacilityImportRunner} refuse délibérément de
 * purger la démo tant qu'aucune donnée réelle ne l'a remplacée (jamais vider
 * l'annuaire) — ce qui laisse ouvert un scénario distinct qu'aucune des deux
 * gardes seules ne couvre : un import totalement raté (fichier absent,
 * corrompu, ou dont tous les enregistrements sont rejetés) sur une base VIERGE
 * ne déclenche NI la purge (refusée à raison) NI {@link DemoDataProductionGuard}
 * (rien à détecter : aucune donnée démo n'a jamais été chargée). Sans cette
 * garde, l'application démarrerait « avec succès » et servirait un annuaire
 * vide, silencieusement.
 */
@Component
@Profile("production")
@Order(Ordered.LOWEST_PRECEDENCE)
class EmptyDirectoryProductionGuard implements ApplicationRunner {

    private final FacilityDirectoryPort directoryPort;

    EmptyDirectoryProductionGuard(FacilityDirectoryPort directoryPort) {
        this.directoryPort = directoryPort;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!directoryPort.hasNonDemoData()) {
            throw new IllegalStateException(
                    "Aucun établissement (démo exclue) en annuaire : démarrage refusé. "
                            + "Vérifiez facility.import.file (fichier présent, lisible, "
                            + "enregistrements acceptés par la validation).");
        }
    }
}
