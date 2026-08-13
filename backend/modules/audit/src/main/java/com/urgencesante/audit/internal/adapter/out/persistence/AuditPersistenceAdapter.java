package com.urgencesante.audit.internal.adapter.out.persistence;

import com.urgencesante.audit.internal.adapter.out.persistence.mapper.AuditEntryEntityMapper;
import com.urgencesante.audit.internal.adapter.out.persistence.repository.AuditEntrySpringRepository;
import com.urgencesante.audit.internal.application.port.out.SaveAuditEntryPort;
import com.urgencesante.audit.internal.domain.model.AuditEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptateur de persistance idempotent : une ligne déjà connue (même
 * {@code source_event_id}) n'est jamais réécrite.
 *
 * <p>{@code REQUIRES_NEW} est nécessaire, pas cosmétique : l'appelant est un
 * {@code @TransactionalEventListener(phase = AFTER_COMMIT)}, exécuté alors
 * que les ressources de la transaction du relais outbox sont encore liées au
 * thread (le nettoyage Spring n'intervient qu'après {@code afterCommit()}).
 * Sans {@code REQUIRES_NEW}, cet appel « participerait » silencieusement à
 * cette transaction déjà committée au lieu d'en ouvrir une nouvelle : le
 * {@code save} s'exécute sans lever d'exception mais n'est jamais validé —
 * constaté en test réel (issue #136), pas une précaution théorique.
 *
 * <p>La vérification préalable ({@code existsById}) couvre le cas normal ;
 * la capture de {@link DataIntegrityViolationException} est un filet pour la
 * fenêtre de course entre la vérification et l'écriture (deux passages de
 * relais concurrents sur le même événement) — la contrainte d'unicité en
 * base (clé primaire {@code source_event_id}) reste la garantie réelle.
 * {@code saveAndFlush} — pas {@code save} — est nécessaire pour que ce filet
 * fonctionne : l'identifiant est assigné manuellement (pas de
 * {@code @GeneratedValue}), donc Spring Data route l'écriture vers
 * {@code entityManager.merge(...)} plutôt que {@code persist(...)} ; sans
 * flush explicite, l'INSERT réel n'est exécuté qu'au commit de la
 * transaction {@code REQUIRES_NEW} — après le retour de cette méthode, donc
 * hors du {@code catch} ci-dessous.
 */
@Component
public class AuditPersistenceAdapter implements SaveAuditEntryPort {

    private static final Logger LOG = LoggerFactory.getLogger(AuditPersistenceAdapter.class);

    private final AuditEntrySpringRepository repository;
    private final AuditEntryEntityMapper mapper;

    public AuditPersistenceAdapter(AuditEntrySpringRepository repository, AuditEntryEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveIfAbsent(AuditEntry entry) {
        if (repository.existsById(entry.id())) {
            LOG.debug("Ligne d'audit déjà connue pour l'événement {} — ignorée.", entry.id());
            return;
        }
        try {
            repository.saveAndFlush(mapper.toEntity(entry));
        } catch (DataIntegrityViolationException alreadyRecorded) {
            LOG.debug("Ligne d'audit déjà insérée entre-temps pour l'événement {} — ignorée.", entry.id());
        }
    }
}
