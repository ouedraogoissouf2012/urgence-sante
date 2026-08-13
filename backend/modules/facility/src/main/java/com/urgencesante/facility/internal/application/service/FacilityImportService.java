package com.urgencesante.facility.internal.application.service;

import com.urgencesante.facility.internal.application.port.in.ImportFacilitiesUseCase;
import com.urgencesante.facility.internal.application.port.out.FacilityDirectoryPort;
import com.urgencesante.facility.internal.application.port.out.KnownServicePort;
import com.urgencesante.facility.internal.domain.directory.DataStatus;
import com.urgencesante.facility.internal.domain.directory.FacilityImportRecord;
import com.urgencesante.facility.internal.domain.directory.FacilityImportValidator;
import com.urgencesante.facility.internal.domain.directory.ImportReport;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Importe un lot d'établissements de façon IDEMPOTENTE et validée.
 *
 * <p>Chaque enregistrement est validé (traçabilité, zone d'Abidjan, téléphone,
 * services connus), dédupliqué DANS le lot (clé naturelle et nom+position
 * proches), puis inséré ou mis à jour par sa clé naturelle — rejouer le même
 * lot ne crée pas de doublon. En profil production, les données DÉMO (fictives)
 * sont refusées : elles ne peuvent pas être chargées.
 *
 * <p><b>Le lot n'est PAS atomique</b> : chaque enregistrement accepté est
 * persisté par son propre appel à {@link FacilityDirectoryPort#upsert}
 * (transaction indépendante côté adaptateur), traité séquentiellement dans la
 * boucle ci-dessous. Si le traitement s'interrompt en cours de lot (exception,
 * perte de connexion base, processus tué), les enregistrements déjà upsertés
 * RESTENT commités ; ceux non encore atteints ne sont simplement pas traités —
 * c'est une <b>progression partielle</b>, assumée plutôt que cachée derrière
 * une fausse promesse d'atomicité globale.
 *
 * <p>C'est acceptable, et même sans conséquence opérationnelle, PARCE QUE
 * chaque enregistrement est individuellement idempotent (upsert par clé
 * naturelle {@code source}/{@code external_ref}, voir {@link
 * FacilityDirectoryPort#upsert}) : <b>rejouer le lot COMPLET</b> après une
 * interruption ne crée aucun doublon — les enregistrements déjà appliqués
 * redeviennent des mises à jour no-op (mêmes valeurs), et ceux manqués sont
 * appliqués à leur tour. La réparation d'une progression partielle est donc
 * un simple rejeu du lot d'origine, pas une procédure de reprise dédiée.
 */
public class FacilityImportService implements ImportFacilitiesUseCase {

    /** ~100 m en degrés de latitude (seuil de quasi-doublon). */
    private static final double NEAR_DUPLICATE_DEGREES = 0.001;

    private final FacilityDirectoryPort directoryPort;
    private final FacilityImportValidator validator;
    private final boolean productionProfile;

    public FacilityImportService(
            FacilityDirectoryPort directoryPort,
            KnownServicePort knownServicePort,
            boolean productionProfile) {
        this.directoryPort = Objects.requireNonNull(directoryPort);
        this.validator = new FacilityImportValidator(
                Objects.requireNonNull(knownServicePort)::isKnown);
        this.productionProfile = productionProfile;
    }

    @Override
    public ImportReport importDirectory(List<FacilityImportRecord> records) {
        int inserted = 0;
        int updated = 0;
        final List<ImportReport.Rejection> rejected = new ArrayList<>();
        final Set<String> seenKeys = new HashSet<>();
        final List<FacilityImportRecord> accepted = new ArrayList<>();

        for (final FacilityImportRecord record : records) {
            final List<String> reasons = new ArrayList<>(validator.validate(record));

            // Données fictives interdites en production.
            if (productionProfile && record.dataStatus() == DataStatus.DEMO) {
                reasons.add("données de démonstration interdites en production");
            }
            // Doublon de clé naturelle dans le lot.
            final String key = naturalKey(record);
            if (key != null && !seenKeys.add(key)) {
                reasons.add("doublon dans le lot (même source/référence)");
            }
            // Quasi-doublon (même nom, position très proche) dans le lot.
            if (isNearDuplicate(record, accepted)) {
                reasons.add("quasi-doublon d'un établissement déjà présent dans le lot");
            }

            if (!reasons.isEmpty()) {
                rejected.add(new ImportReport.Rejection(
                        record.source(), record.externalRef(), record.name(), reasons));
                continue;
            }

            final boolean exists =
                    directoryPort.existsByNaturalKey(record.source(), record.externalRef());
            directoryPort.upsert(record);
            accepted.add(record);
            if (exists) {
                updated++;
            } else {
                inserted++;
            }
        }
        return new ImportReport(inserted, updated, rejected);
    }

    private static String naturalKey(FacilityImportRecord record) {
        if (record.source() == null || record.externalRef() == null) {
            return null;
        }
        return record.source() + ' ' + record.externalRef();
    }

    private static boolean isNearDuplicate(
            FacilityImportRecord candidate, List<FacilityImportRecord> accepted) {
        final String name = normalize(candidate.name());
        for (final FacilityImportRecord other : accepted) {
            if (normalize(other.name()).equals(name)
                    && Math.abs(other.latitude() - candidate.latitude()) < NEAR_DUPLICATE_DEGREES
                    && Math.abs(other.longitude() - candidate.longitude()) < NEAR_DUPLICATE_DEGREES) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
