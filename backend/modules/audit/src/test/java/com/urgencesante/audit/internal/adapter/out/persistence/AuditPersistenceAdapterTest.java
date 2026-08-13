package com.urgencesante.audit.internal.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.urgencesante.audit.internal.adapter.out.persistence.entity.AuditEntryJpaEntity;
import com.urgencesante.audit.internal.adapter.out.persistence.mapper.AuditEntryEntityMapper;
import com.urgencesante.audit.internal.adapter.out.persistence.repository.AuditEntrySpringRepository;
import com.urgencesante.audit.internal.domain.model.AuditEntry;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class AuditPersistenceAdapterTest {

    @Mock
    private AuditEntrySpringRepository repository;

    @Mock
    private AuditEntryEntityMapper mapper;

    private AuditPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AuditPersistenceAdapter(repository, mapper);
    }

    private static AuditEntry entry(UUID id) {
        return AuditEntry.of(id, "AVAILABILITY_UPDATED", "FACILITY_SERVICE_AVAILABILITY",
                "f1:maternity", null, null, "corr-1", Instant.parse("2026-01-01T12:00:00Z"));
    }

    @Test
    void insere_une_ligne_absente() {
        final UUID id = UUID.randomUUID();
        final AuditEntry entry = entry(id);
        final AuditEntryJpaEntity mapped = new AuditEntryJpaEntity(
                id, "AVAILABILITY_UPDATED", "FACILITY_SERVICE_AVAILABILITY", "f1:maternity",
                null, null, "corr-1", Instant.parse("2026-01-01T12:00:00Z"));
        given(repository.existsById(id)).willReturn(false);
        given(mapper.toEntity(entry)).willReturn(mapped);

        adapter.saveIfAbsent(entry);

        then(repository).should().saveAndFlush(mapped);
    }

    @Test
    void ignore_silencieusement_une_ligne_deja_connue() {
        final UUID id = UUID.randomUUID();
        given(repository.existsById(id)).willReturn(true);

        adapter.saveIfAbsent(entry(id));

        then(repository).should(never()).saveAndFlush(any());
        then(mapper).should(never()).toEntity(any());
    }

    @Test
    void absorbe_une_violation_de_contrainte_survenue_entre_la_verification_et_l_ecriture() {
        final UUID id = UUID.randomUUID();
        final AuditEntry entry = entry(id);
        given(repository.existsById(id)).willReturn(false);
        given(mapper.toEntity(entry)).willReturn(new AuditEntryJpaEntity(
                id, "AVAILABILITY_UPDATED", "FACILITY_SERVICE_AVAILABILITY", "f1:maternity",
                null, null, "corr-1", Instant.parse("2026-01-01T12:00:00Z")));
        given(repository.saveAndFlush(any())).willThrow(new DataIntegrityViolationException("doublon"));

        assertThatCode(() -> adapter.saveIfAbsent(entry)).doesNotThrowAnyException();
    }
}
