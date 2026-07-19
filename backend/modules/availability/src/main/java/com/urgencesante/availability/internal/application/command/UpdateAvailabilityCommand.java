package com.urgencesante.availability.internal.application.command;

import com.urgencesante.availability.internal.domain.model.AvailabilityStatus;
import java.util.Objects;
import java.util.UUID;

/**
 * Commande de mise à jour du statut d'un service pour un établissement.
 *
 * @param correlationId identifiant de corrélation de la requête d'origine
 *     (propagé jusqu'à l'événement), ou {@code null} si indisponible
 */
public record UpdateAvailabilityCommand(
        UUID facilityId, String serviceCode, AvailabilityStatus status, String correlationId) {

    public UpdateAvailabilityCommand {
        Objects.requireNonNull(facilityId, "L'établissement est requis");
        Objects.requireNonNull(serviceCode, "Le service est requis");
        Objects.requireNonNull(status, "Le statut est requis");
    }

    /** Commande sans corrélation explicite (tests, appels internes). */
    public UpdateAvailabilityCommand(UUID facilityId, String serviceCode, AvailabilityStatus status) {
        this(facilityId, serviceCode, status, null);
    }
}
