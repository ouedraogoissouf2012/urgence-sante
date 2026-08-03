package com.urgencesante.patient.internal;

import com.urgencesante.patient.PatientFacade;
import com.urgencesante.patient.internal.application.port.out.PatientSessionPort;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Implémente l'API publique du module en déléguant aux ports internes. */
@Component
public class PatientFacadeAdapter implements PatientFacade {

    private final PatientSessionPort sessions;

    public PatientFacadeAdapter(PatientSessionPort sessions) {
        this.sessions = sessions;
    }

    @Override
    public Optional<UUID> authenticate(String rawToken) {
        return sessions.resolvePatient(rawToken);
    }
}
