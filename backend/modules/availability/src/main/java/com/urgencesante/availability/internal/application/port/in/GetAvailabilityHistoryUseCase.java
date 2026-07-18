package com.urgencesante.availability.internal.application.port.in;

import com.urgencesante.availability.internal.application.result.AvailabilityHistoryEntry;
import java.util.List;
import java.util.UUID;

/** Port entrant : historique des mises à jour d'un service (récent d'abord). */
public interface GetAvailabilityHistoryUseCase {

    List<AvailabilityHistoryEntry> history(UUID facilityId, String serviceCode, int limit);
}
