package com.urgencesante.availability.internal.domain.exception;

import java.util.UUID;

/** Le service demandé n'est pas offert par l'établissement visé. */
public class ServiceNotOfferedException extends RuntimeException {

    public ServiceNotOfferedException(UUID facilityId, String serviceCode) {
        super("Le service « " + serviceCode + " » n'est pas offert par l'établissement " + facilityId);
    }
}
