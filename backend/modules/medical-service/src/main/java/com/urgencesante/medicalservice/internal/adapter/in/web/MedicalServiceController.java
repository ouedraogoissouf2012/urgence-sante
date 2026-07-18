package com.urgencesante.medicalservice.internal.adapter.in.web;

import com.urgencesante.medicalservice.internal.adapter.in.web.dto.response.MedicalServiceResponse;
import com.urgencesante.medicalservice.internal.adapter.in.web.mapper.MedicalServiceWebMapper;
import com.urgencesante.medicalservice.internal.application.port.in.ListMedicalServicesUseCase;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptateur entrant REST du catalogue. Conforme au contrat OpenAPI. Aucune
 * logique métier : validation, mapping et appel du port entrant.
 */
@RestController
@RequestMapping("/api/v1/medical-services")
public class MedicalServiceController {

    private final ListMedicalServicesUseCase listMedicalServices;
    private final MedicalServiceWebMapper mapper;

    public MedicalServiceController(
            ListMedicalServicesUseCase listMedicalServices, MedicalServiceWebMapper mapper) {
        this.listMedicalServices = listMedicalServices;
        this.mapper = mapper;
    }

    @GetMapping
    public List<MedicalServiceResponse> list(@RequestParam(required = false) String category) {
        return listMedicalServices.list(Optional.ofNullable(category)).stream()
                .map(mapper::toResponse)
                .toList();
    }
}
