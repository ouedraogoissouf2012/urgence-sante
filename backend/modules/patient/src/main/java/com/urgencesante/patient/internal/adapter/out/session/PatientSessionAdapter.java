package com.urgencesante.patient.internal.adapter.out.session;

import com.urgencesante.buildingblocks.security.TokenHasher;
import com.urgencesante.patient.internal.adapter.out.persistence.entity.PatientSessionJpaEntity;
import com.urgencesante.patient.internal.adapter.out.persistence.repository.PatientSessionSpringRepository;
import com.urgencesante.patient.internal.application.port.out.PatientSessionPort;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * Émission de jetons de session patient.
 *
 * <p>Génère 32 octets aléatoires (SecureRandom) encodés URL-safe, persiste
 * l'EMPREINTE SHA-256 (jamais le jeton en clair) avec une expiration, et renvoie
 * le jeton en clair une seule fois. Un jeton perdu n'est donc pas récupérable
 * depuis la base.
 *
 * <p>Non annoté {@code @Component} : instancié par {@code PatientConfiguration}
 * (qui lui fournit la durée de validité configurable).
 */
public class PatientSessionAdapter implements PatientSessionPort {

    private static final int TOKEN_BYTES = 32;

    private final PatientSessionSpringRepository repository;
    private final Clock clock;
    private final Duration validity;
    private final SecureRandom random = new SecureRandom();

    public PatientSessionAdapter(
            PatientSessionSpringRepository repository, Clock clock, Duration validity) {
        this.repository = repository;
        this.clock = clock;
        this.validity = validity;
    }

    @Override
    public String issueToken(UUID patientId) {
        final byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        final String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        final var now = clock.instant();
        repository.save(PatientSessionJpaEntity.of(
                UUID.randomUUID(), patientId, TokenHasher.sha256Hex(rawToken),
                now, now.plus(validity)));
        return rawToken;
    }

    @Override
    public Optional<UUID> resolvePatient(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }
        return repository.findByTokenHash(TokenHasher.sha256Hex(rawToken.trim()))
                .filter(session -> session.expiresAt().isAfter(clock.instant()))
                .map(PatientSessionJpaEntity::patientId);
    }

    @Override
    public int purgeExpired() {
        return repository.deleteByExpiresAtBefore(clock.instant());
    }
}
