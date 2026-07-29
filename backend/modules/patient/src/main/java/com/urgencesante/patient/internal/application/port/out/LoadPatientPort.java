package com.urgencesante.patient.internal.application.port.out;

import com.urgencesante.patient.internal.domain.model.PatientAccount;
import com.urgencesante.patient.internal.domain.model.PhoneNumber;
import java.util.Optional;

/** Lecture des comptes patients (port sortant). */
public interface LoadPatientPort {

    Optional<PatientAccount> findByPhone(PhoneNumber phone);

    boolean existsByPhone(PhoneNumber phone);
}
