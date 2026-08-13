package com.urgencesante.patient.internal.adapter.out.persistence.repository;

import com.urgencesante.patient.internal.adapter.out.persistence.entity.PatientSessionJpaEntity;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PatientSessionSpringRepository
        extends JpaRepository<PatientSessionJpaEntity, UUID> {

    java.util.Optional<PatientSessionJpaEntity> findByTokenHash(String tokenHash);

    /** Suppression en masse (une seule instruction SQL) des sessions expirées. */
    @Modifying
    @Transactional
    @Query("DELETE FROM PatientSessionJpaEntity s WHERE s.expiresAt < :now")
    int deleteByExpiresAtBefore(@Param("now") Instant now);

    /** Révocation par empreinte de jeton. 0 si déjà absente (idempotent). */
    @Modifying
    @Transactional
    @Query("DELETE FROM PatientSessionJpaEntity s WHERE s.tokenHash = :tokenHash")
    int deleteByTokenHash(@Param("tokenHash") String tokenHash);
}
