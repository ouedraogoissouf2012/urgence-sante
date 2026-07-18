package com.urgencesante.availability.internal.application.command;

import com.urgencesante.availability.internal.domain.model.AvailabilityStatus;
import java.util.Objects;
import java.util.UUID;

/** Commande de mise à jour du statut d'un service pour un établissement. */
public record UpdateAvailabilityCommand(UUID facilityId, String serviceCode, AvailabilityStatus status) {

    public UpdateAvailabilityCommand {
        Objects.requireNonNull(facilityId, "L'établissement est requis");
        Objects.requireNonNull(serviceCode, "Le service est requis");
        Objects.requireNonNull(status, "Le statut est requis");
    }
}
