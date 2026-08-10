package com.urgencesante.patient.internal.adapter.out.session;

import com.urgencesante.patient.internal.application.port.out.PatientSessionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Purge périodique des sessions patient expirées (issue #132) : sans elle,
 * {@code patient_session} grossit sans fin (chaque connexion/inscription
 * insère une ligne, jamais effacée).
 *
 * <p>Aucun risque d'intégrité (une session expirée est déjà inutilisable par
 * {@link PatientSessionAdapter#resolvePatient}) — uniquement une dette de
 * stockage. Le nettoyage peut donc rester peu fréquent.
 */
@Component
public class PatientSessionPurgeJob {

    private static final Logger LOG = LoggerFactory.getLogger(PatientSessionPurgeJob.class);

    private final PatientSessionPort sessions;

    public PatientSessionPurgeJob(PatientSessionPort sessions) {
        this.sessions = sessions;
    }

    @Scheduled(fixedDelayString = "${patient.session.purge-delay-ms:3600000}")
    public void purge() {
        purgeOnce();
    }

    /** Un passage de purge (extrait pour les tests). Retourne le nombre supprimé. */
    public int purgeOnce() {
        final int purged = sessions.purgeExpired();
        if (purged > 0) {
            LOG.info("Purge de {} session(s) patient expirée(s)", purged);
        }
        return purged;
    }
}
