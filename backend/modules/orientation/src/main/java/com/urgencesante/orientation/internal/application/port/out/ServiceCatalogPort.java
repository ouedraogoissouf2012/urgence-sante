package com.urgencesante.orientation.internal.application.port.out;

/** Port sortant : vérification de l'existence d'un service au catalogue. */
public interface ServiceCatalogPort {

    boolean exists(String serviceCode);
}
