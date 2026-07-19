package com.urgencesante.identity.internal.adapter.out.persistence.repository;

import com.urgencesante.identity.internal.adapter.out.persistence.entity.PortalCredentialJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository des identifiants du portail. */
public interface PortalCredentialSpringRepository
        extends JpaRepository<PortalCredentialJpaEntity, UUID> {

    Optional<PortalCredentialJpaEntity> findByTokenHashAndActiveIsTrue(String tokenHash);
}
