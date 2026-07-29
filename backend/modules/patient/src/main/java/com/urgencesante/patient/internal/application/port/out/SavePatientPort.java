package com.urgencesante.patient.internal.application.port.out;

import com.urgencesante.patient.internal.domain.model.PatientAccount;

/** Persistance des comptes patients (port sortant). */
public interface SavePatientPort {

    void save(PatientAccount account);
}
