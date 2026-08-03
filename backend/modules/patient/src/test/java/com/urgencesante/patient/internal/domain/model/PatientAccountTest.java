package com.urgencesante.patient.internal.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.urgencesante.patient.internal.domain.exception.PatientValidationException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PatientAccountTest {

    private static final UUID ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final PhoneNumber PHONE = PhoneNumber.of("+2250102030405");
    private static final Instant NOW = Instant.parse("2026-07-27T10:00:00Z");

    @Test
    void cree_un_compte_actif_avec_l_empreinte_du_mot_de_passe() {
        final PatientAccount account = PatientAccount.register(ID, PHONE, "hash-bcrypt", NOW);

        assertThat(account.id()).isEqualTo(ID);
        assertThat(account.phone()).isEqualTo(PHONE);
        assertThat(account.passwordHash()).isEqualTo("hash-bcrypt");
        assertThat(account.active()).isTrue();
        assertThat(account.createdAt()).isEqualTo(NOW);
    }

    @Test
    void refuse_un_hash_de_mot_de_passe_absent() {
        assertThatThrownBy(() -> PatientAccount.register(ID, PHONE, "  ", NOW))
                .isInstanceOf(PatientValidationException.class);
    }

    @Test
    void deux_comptes_sont_egaux_par_identifiant() {
        final PatientAccount a = PatientAccount.register(ID, PHONE, "h1", NOW);
        final PatientAccount b = PatientAccount.register(
                ID, PhoneNumber.of("+2250999999999"), "h2", NOW.plusSeconds(60));

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
