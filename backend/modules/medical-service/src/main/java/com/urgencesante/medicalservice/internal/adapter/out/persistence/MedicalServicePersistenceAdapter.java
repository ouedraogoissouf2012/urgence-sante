package com.urgencesante.medicalservice.internal.adapter.out.persistence;

import com.urgencesante.medicalservice.internal.adapter.out.persistence.entity.MedicalServiceJpaEntity;
import com.urgencesante.medicalservice.internal.adapter.out.persistence.mapper.MedicalServiceEntityMapper;
import com.urgencesante.medicalservice.internal.adapter.out.persistence.repository.MedicalServiceSpringRepository;
import com.urgencesante.medicalservice.internal.application.port.out.LoadMedicalServicePort;
import com.urgencesante.medicalservice.internal.domain.model.MedicalService;
import com.urgencesante.medicalservice.internal.domain.model.MedicalServiceCode;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Adaptateur de persistance : implémente le port sortant du catalogue. */
@Component
public class MedicalServicePersistenceAdapter implements LoadMedicalServicePort {

    private final MedicalServiceSpringRepository repository;
    private final MedicalServiceEntityMapper mapper;

    public MedicalServicePersistenceAdapter(
            MedicalServiceSpringRepository repository, MedicalServiceEntityMapper mapper) {
        this.repository = Objects.requireNonNull(repository);
        this.mapper = Objects.requireNonNull(mapper);
    }

    @Override
    public List<MedicalService> findAll(Optional<String> category) {
        final List<MedicalServiceJpaEntity> entities = category
                .map(value -> repository.findByCategoryOrderByLabelAsc(value.trim().toLowerCase(Locale.ROOT)))
                .orElseGet(repository::findAllByOrderByLabelAsc);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<MedicalService> findByCode(MedicalServiceCode code) {
        return repository.findById(code.value()).map(mapper::toDomain);
    }
}
