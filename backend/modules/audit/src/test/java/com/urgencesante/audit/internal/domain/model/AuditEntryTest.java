package com.urgencesante.audit.internal.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.urgencesante.audit.internal.domain.exception.AuditValidationException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuditEntryTest {

    private static final UUID AN_ID = UUID.randomUUID();
    private static final Instant WHEN = Instant.parse("2026-01-01T12:00:00Z");

    @Test
    void construit_une_ligne_valide_avec_acteur_absent() {
        final AuditEntry entry = AuditEntry.of(
                AN_ID, "AVAILABILITY_UPDATED", "FACILITY_SERVICE_AVAILABILITY",
                "f1:maternity", null, null, "corr-1", WHEN);

        assertThat(entry.id()).isEqualTo(AN_ID);
        assertThat(entry.action()).isEqualTo("AVAILABILITY_UPDATED");
        assertThat(entry.actorId()).isEmpty();
        assertThat(entry.actorLabel()).isEmpty();
        assertThat(entry.occurredAt()).isEqualTo(WHEN);
    }

    @Test
    void conserve_l_acteur_quand_il_est_fourni() {
        final AuditEntry entry = AuditEntry.of(
                AN_ID, "AVAILABILITY_UPDATED", "FACILITY_SERVICE_AVAILABILITY",
                "f1:maternity", "agent-42", "  Dr Koné  ", "corr-1", WHEN);

        assertThat(entry.actorId()).contains("agent-42");
        assertThat(entry.actorLabel()).contains("Dr Koné");
    }

    @Test
    void refuse_une_action_vide() {
        assertThatThrownBy(() -> AuditEntry.of(
                AN_ID, "   ", "FACILITY_SERVICE_AVAILABILITY", "f1:maternity", null, null, "corr-1", WHEN))
                .isInstanceOf(AuditValidationException.class)
                .hasMessageContaining("action");
    }

    @Test
    void refuse_un_identifiant_de_ressource_vide() {
        assertThatThrownBy(() -> AuditEntry.of(
                AN_ID, "AVAILABILITY_UPDATED", "FACILITY_SERVICE_AVAILABILITY", "", null, null, "corr-1", WHEN))
                .isInstanceOf(AuditValidationException.class)
                .hasMessageContaining("ressource");
    }

    @Test
    void l_egalite_repose_sur_l_identite() {
        final AuditEntry a = AuditEntry.of(
                AN_ID, "AVAILABILITY_UPDATED", "FACILITY_SERVICE_AVAILABILITY", "f1:maternity",
                null, null, "corr-1", WHEN);
        final AuditEntry b = AuditEntry.of(
                AN_ID, "AVAILABILITY_UPDATED", "FACILITY_SERVICE_AVAILABILITY", "f2:surgery",
                "someone", "Someone", "corr-2", Instant.now());

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }
}
