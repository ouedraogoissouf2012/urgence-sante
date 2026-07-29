package com.urgencesante.patient.internal.adapter.out.persistence.repository;

import com.urgencesante.patient.internal.adapter.out.persistence.entity.PatientSessionJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientSessionSpringRepository
        extends JpaRepository<PatientSessionJpaEntity, UUID> {

    java.util.Optional<PatientSessionJpaEntity> findByTokenHash(String tokenHash);
}
