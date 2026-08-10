package com.urgencesante.patient.internal.configuration;

import com.urgencesante.patient.internal.adapter.out.persistence.repository.PatientSessionSpringRepository;
import com.urgencesante.patient.internal.adapter.out.security.BCryptPasswordHasher;
import com.urgencesante.patient.internal.adapter.out.session.PatientSessionAdapter;
import com.urgencesante.patient.internal.application.port.out.LoadPatientPort;
import com.urgencesante.patient.internal.application.port.out.PasswordHasherPort;
import com.urgencesante.patient.internal.application.port.out.PatientSessionPort;
import com.urgencesante.patient.internal.application.port.out.SavePatientPort;
import com.urgencesante.patient.internal.application.port.out.TransactionPort;
import com.urgencesante.patient.internal.application.service.PatientService;
import java.time.Clock;
import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Assemblage Spring du module Patient. L'horloge est fournie par le bootstrap.
 * Le coût BCrypt et la durée de session sont configurables (valeurs par défaut
 * saines) ; le générateur d'identifiants est isolé pour la testabilité.
 */
@Configuration
public class PatientConfiguration {

    @Bean
    PasswordHasherPort passwordHasher(
            @Value("${patient.password.bcrypt-strength:12}") int strength) {
        return new BCryptPasswordHasher(strength);
    }

    @Bean
    PatientSessionPort patientSessionPort(
            PatientSessionSpringRepository sessionRepository,
            Clock clock,
            @Value("${patient.session.validity-days:90}") long validityDays) {
        return new PatientSessionAdapter(sessionRepository, clock, Duration.ofDays(validityDays));
    }

    @Bean
    Supplier<UUID> patientIdGenerator() {
        return UUID::randomUUID;
    }

    @Bean
    PatientService patientService(
            LoadPatientPort loadPatient,
            SavePatientPort savePatient,
            PasswordHasherPort passwordHasher,
            PatientSessionPort sessions,
            TransactionPort transactionPort,
            Clock clock,
            Supplier<UUID> patientIdGenerator) {
        return new PatientService(
                loadPatient, savePatient, passwordHasher, sessions, transactionPort,
                clock, patientIdGenerator);
    }
}
