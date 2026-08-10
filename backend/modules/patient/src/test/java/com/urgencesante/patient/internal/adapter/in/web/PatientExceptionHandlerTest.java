package com.urgencesante.patient.internal.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.urgencesante.patient.internal.domain.exception.InvalidCredentialsException;
import com.urgencesante.patient.internal.domain.exception.PatientValidationException;
import com.urgencesante.patient.internal.domain.exception.PhoneAlreadyRegisteredException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

class PatientExceptionHandlerTest {

    private final PatientExceptionHandler handler = new PatientExceptionHandler();

    /**
     * Issue #131 : une violation de contrainte d'unicité en course (le contrôle
     * applicatif existsByPhone n'a rien vu, la base a rejeté l'insertion) doit
     * produire la MÊME réponse 409 que le cas non concurrent — pas une fuite
     * d'erreur interne (500).
     */
    @Test
    void une_violation_de_contrainte_d_unicite_devient_un_409_identique_au_doublon_non_concurrent() {
        final ProblemDetail fromRace =
                handler.handleIntegrityViolation(new DataIntegrityViolationException("phone unique violated"));
        final ProblemDetail fromAppCheck = handler.handleConflict(new PhoneAlreadyRegisteredException());

        assertThat(fromRace.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(fromRace.getTitle()).isEqualTo(fromAppCheck.getTitle());
        assertThat(fromRace.getDetail()).isEqualTo(fromAppCheck.getDetail());
    }

    @Test
    void une_erreur_de_validation_devient_un_400() {
        final ProblemDetail problem =
                handler.handleBadRequest(new PatientValidationException("téléphone invalide"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getDetail()).isEqualTo("téléphone invalide");
    }

    @Test
    void un_doublon_applicatif_devient_un_409() {
        final ProblemDetail problem = handler.handleConflict(new PhoneAlreadyRegisteredException());

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problem.getTitle()).isEqualTo("Numéro déjà utilisé");
    }

    @Test
    void des_identifiants_invalides_deviennent_un_401() {
        final ProblemDetail problem = handler.handleUnauthorized(new InvalidCredentialsException());

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }
}
