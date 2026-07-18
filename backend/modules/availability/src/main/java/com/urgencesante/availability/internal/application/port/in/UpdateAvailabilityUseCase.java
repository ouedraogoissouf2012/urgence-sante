package com.urgencesante.availability.internal.application.port.in;

import com.urgencesante.availability.internal.application.command.UpdateAvailabilityCommand;
import com.urgencesante.availability.internal.application.result.ServiceAvailabilitySnapshot;

/** Port entrant : mettre à jour le statut d'un service (horodaté, tracé, publié). */
public interface UpdateAvailabilityUseCase {

    ServiceAvailabilitySnapshot update(UpdateAvailabilityCommand command);
}
