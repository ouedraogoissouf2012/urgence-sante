package com.urgencesante.patient.internal.application.port.out;

import com.urgencesante.patient.internal.domain.model.PatientAccount;
import com.urgencesante.patient.internal.domain.model.PhoneNumber;
import java.util.Optional;

/** Lecture des comptes patients (port sortant). */
public interface LoadPatientPort {

    Optional<PatientAccount> findByPhone(PhoneNumber phone);

    /**
     * Existence PAR NUMÉRO, sans filtrer sur {@code active}.
     *
     * <p><b>Décision produit non tranchée</b> : rien dans le domaine ne
     * désactive jamais un compte aujourd'hui ({@link PatientAccount#register}
     * crée toujours {@code active = true} ; aucun cas d'usage n'écrit {@code
     * active = false}). Le jour où une désactivation existera (RGPD, fraude,
     * demande utilisateur…), ce comportement devient une IMPASSE : {@link
     * com.urgencesante.patient.internal.application.service.PatientService#register}
     * appelle cette méthode et refuse l'inscription si elle répond {@code
     * true}, MÊME pour un compte désactivé — ce numéro ne pourra alors plus
     * jamais se réinscrire, sans recours.
     *
     * <p>Deux résolutions possibles, à trancher au moment d'implémenter la
     * désactivation (produit, pas technique) :
     * <ul>
     *   <li><b>Réactivation</b> : {@code register} sur un numéro désactivé
     *       réactive le compte existant au lieu d'en créer un nouveau
     *       (préserve l'historique, mais un tiers qui obtient ce numéro de
     *       téléphone après portabilité récupérerait l'ancien compte) ;</li>
     *   <li><b>Unicité partielle</b> : contrainte d'unicité sur {@code phone}
     *       restreinte à {@code WHERE active}, et {@code existsByPhone}
     *       filtre également sur {@code active} — un numéro désactivé
     *       redevient immédiatement disponible pour une inscription neuve
     *       (perd le lien avec l'ancien compte et son historique).</li>
     * </ul>
     */
    boolean existsByPhone(PhoneNumber phone);
}
