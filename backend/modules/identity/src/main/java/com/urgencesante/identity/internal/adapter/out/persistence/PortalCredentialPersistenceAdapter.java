package com.urgencesante.identity.internal.adapter.out.persistence;

import com.urgencesante.identity.internal.adapter.out.persistence.repository.PortalCredentialSpringRepository;
import com.urgencesante.identity.internal.application.port.out.LoadCredentialPort;
import com.urgencesante.identity.internal.domain.model.PortalCredential;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** Adaptateur de persistance des identifiants du portail. */
@Component
class PortalCredentialPersistenceAdapter implements LoadCredentialPort {

    private final PortalCredentialSpringRepository repository;

    PortalCredentialPersistenceAdapter(PortalCredentialSpringRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public Optional<PortalCredential> findActiveByTokenHash(String tokenHash) {
        return repository.findByTokenHashAndActiveIsTrue(tokenHash)
                .map(entity -> entity.toDomain());
    }
}
