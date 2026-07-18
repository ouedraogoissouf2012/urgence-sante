package com.urgencesante.medicalservice;

import java.util.List;
import java.util.Optional;

/**
 * API publique du module Medical Service. Permet aux autres modules (ex.
 * orientation) de consulter et valider le catalogue, sans accéder à son interne.
 */
public interface MedicalServiceFacade {

    /** Vue publique d'un service par son code, si présent au catalogue. */
    Optional<MedicalServiceView> findByCode(String code);

    /** Vrai si le code correspond à un service du catalogue. */
    boolean exists(String code);

    /** Catalogue complet, trié par libellé. */
    List<MedicalServiceView> all();
}
