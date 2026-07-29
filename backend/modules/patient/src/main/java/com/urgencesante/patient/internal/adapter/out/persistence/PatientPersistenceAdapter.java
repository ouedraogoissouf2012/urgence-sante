package com.urgencesante.patient.internal.adapter.out.persistence;

import com.urgencesante.patient.internal.adapter.out.persistence.entity.PatientAccountJpaEntity;
import com.urgencesante.patient.internal.adapter.out.persistence.repository.PatientAccountSpringRepository;
import com.urgencesante.patient.internal.application.port.out.LoadPatientPort;
import com.urgencesante.patient.internal.application.port.out.SavePatientPort;
import com.urgencesante.patient.internal.domain.model.PatientAccount;
import com.urgencesante.patient.internal.domain.model.PhoneNumber;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Persistance des comptes patients (JPA). */
@Component
public class PatientPersistenceAdapter implements LoadPatientPort, SavePatientPort {

    private final PatientAccountSpringRepository repository;

    public PatientPersistenceAdapter(PatientAccountSpringRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PatientAccount> findByPhone(PhoneNumber phone) {
        return repository.findByPhone(phone.value()).map(PatientAccountJpaEntity::toDomain);
    }

    @Override
    public boolean existsByPhone(PhoneNumber phone) {
        return repository.existsByPhone(phone.value());
    }

    @Override
    public void save(PatientAccount account) {
        repository.save(PatientAccountJpaEntity.fromDomain(account));
    }
}
