package com.urgencesante.patient.internal.adapter.out.session;

import static org.assertj.core.api.Assertions.assertThat;

import com.urgencesante.patient.internal.application.port.out.PatientSessionPort;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PatientSessionPurgeJobTest {

    /** Faux port de sessions : ne modélise que ce dont le job a besoin. */
    private static final class FakeSessions implements PatientSessionPort {
        private int expiredCount;
        int purgeCalls;

        @Override
        public String issueToken(UUID patientId) {
            throw new UnsupportedOperationException("non sollicité par ce test");
        }

        @Override
        public Optional<UUID> resolvePatient(String rawToken) {
            throw new UnsupportedOperationException("non sollicité par ce test");
        }

        @Override
        public int purgeExpired() {
            purgeCalls++;
            final int purged = expiredCount;
            expiredCount = 0; // une purge vide la file : un second passage ne retrouve rien
            return purged;
        }
    }

    private final FakeSessions sessions = new FakeSessions();
    private final PatientSessionPurgeJob job = new PatientSessionPurgeJob(sessions);

    @Test
    void purge_les_sessions_expirees_et_renvoie_le_nombre_supprime() {
        sessions.expiredCount = 3;

        final int purged = job.purgeOnce();

        assertThat(purged).isEqualTo(3);
        assertThat(sessions.purgeCalls).isEqualTo(1);
    }

    @Test
    void un_second_passage_sans_session_expiree_ne_supprime_rien() {
        sessions.expiredCount = 2;
        job.purgeOnce();

        final int secondPass = job.purgeOnce();

        assertThat(secondPass).isZero();
        assertThat(sessions.purgeCalls).isEqualTo(2);
    }
}
