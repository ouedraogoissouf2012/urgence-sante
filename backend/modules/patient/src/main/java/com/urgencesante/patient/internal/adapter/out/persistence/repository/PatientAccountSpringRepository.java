package com.urgencesante.patient.internal.adapter.out.persistence.repository;

import com.urgencesante.patient.internal.adapter.out.persistence.entity.PatientAccountJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientAccountSpringRepository
        extends JpaRepository<PatientAccountJpaEntity, UUID> {

    Optional<PatientAccountJpaEntity> findByPhone(String phone);

    boolean existsByPhone(String phone);
}
