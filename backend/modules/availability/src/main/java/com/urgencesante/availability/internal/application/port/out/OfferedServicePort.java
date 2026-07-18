package com.urgencesante.availability.internal.application.port.out;

import java.util.UUID;

/**
 * Port sortant : vérifie qu'un établissement offre bien un service donné.
 * Implémenté via l'API publique du module Facility.
 */
public interface OfferedServicePort {

    boolean offers(UUID facilityId, String serviceCode);
}
