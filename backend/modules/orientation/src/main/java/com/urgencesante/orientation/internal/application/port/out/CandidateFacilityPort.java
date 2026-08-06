package com.urgencesante.orientation.internal.application.port.out;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Port sortant : établissements candidats offrant un service, par proximité. */
public interface CandidateFacilityPort {

    /**
     * Candidats offrant AU MOINS UN des services demandés (sémantique OU), triés
     * par proximité.
     */
    List<CandidateFacility> findCandidates(
            Set<String> serviceCodes, double latitude, double longitude, int radiusMeters, int limit);

    /**
     * Établissement candidat (données minimales pour l'évaluation et la fiche).
     *
     * @param phone téléphone du centre, ou {@code null} si inconnu
     */
    record CandidateFacility(
            UUID facilityId, String name, double latitude, double longitude, String phone) {
    }
}
