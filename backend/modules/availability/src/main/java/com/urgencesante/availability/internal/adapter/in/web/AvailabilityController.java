package com.urgencesante.availability.internal.adapter.in.web;

import com.urgencesante.availability.internal.adapter.in.web.dto.request.UpdateAvailabilityRequest;
import com.urgencesante.availability.internal.adapter.in.web.dto.response.AvailabilityHistoryEntryResponse;
import com.urgencesante.availability.internal.adapter.in.web.dto.response.FacilityAvailabilityResponse;
import com.urgencesante.availability.internal.adapter.in.web.dto.response.ServiceAvailabilityResponse;
import com.urgencesante.availability.internal.adapter.in.web.mapper.AvailabilityWebMapper;
import com.urgencesante.availability.internal.application.command.UpdateAvailabilityCommand;
import com.urgencesante.availability.internal.application.port.in.GetAvailabilityHistoryUseCase;
import com.urgencesante.availability.internal.application.port.in.GetFacilityAvailabilityUseCase;
import com.urgencesante.availability.internal.application.port.in.UpdateAvailabilityUseCase;
import com.urgencesante.availability.internal.domain.exception.AvailabilityValidationException;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptateur entrant REST de la disponibilité. Conforme au contrat OpenAPI.
 *
 * <p>Note : la mise à jour sera protégée par le module identity (authentification
 * de l'agent) lorsqu'il sera disponible — dette tracée.
 */
@RestController
@RequestMapping("/api/v1/facilities/{facilityId}/availability")
public class AvailabilityController {

    private final GetFacilityAvailabilityUseCase getFacilityAvailability;
    private final UpdateAvailabilityUseCase updateAvailability;
    private final GetAvailabilityHistoryUseCase getHistory;
    private final AvailabilityWebMapper mapper;

    public AvailabilityController(
            GetFacilityAvailabilityUseCase getFacilityAvailability,
            UpdateAvailabilityUseCase updateAvailability,
            GetAvailabilityHistoryUseCase getHistory,
            AvailabilityWebMapper mapper) {
        this.getFacilityAvailability = getFacilityAvailability;
        this.updateAvailability = updateAvailability;
        this.getHistory = getHistory;
        this.mapper = mapper;
    }

    @GetMapping
    public FacilityAvailabilityResponse get(@PathVariable String facilityId) {
        return mapper.toResponse(getFacilityAvailability.forFacility(parseFacilityId(facilityId)));
    }

    @PutMapping("/{serviceCode}")
    public ServiceAvailabilityResponse update(
            @PathVariable String facilityId,
            @PathVariable String serviceCode,
            @RequestBody UpdateAvailabilityRequest request) {
        if (request == null || request.status() == null) {
            throw new AvailabilityValidationException("Le statut est requis");
        }
        return mapper.toResponse(updateAvailability.update(
                new UpdateAvailabilityCommand(parseFacilityId(facilityId), serviceCode, request.status())));
    }

    @GetMapping("/{serviceCode}/history")
    public List<AvailabilityHistoryEntryResponse> history(
            @PathVariable String facilityId,
            @PathVariable String serviceCode,
            @RequestParam(defaultValue = "20") int limit) {
        if (limit < 1 || limit > 100) {
            throw new AvailabilityValidationException("La limite doit être dans [1, 100]");
        }
        return getHistory.history(parseFacilityId(facilityId), serviceCode, limit).stream()
                .map(entry -> new AvailabilityHistoryEntryResponse(
                        entry.status().name(), entry.updatedAt()))
                .toList();
    }

    private static UUID parseFacilityId(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException exception) {
            throw new AvailabilityValidationException("Identifiant d'établissement invalide : " + raw);
        }
    }
}
