package com.urgencesante.buildingblocks.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

class ExceptionHandlerSupportTest {

    @Test
    void construit_un_problem_detail_avec_statut_detail_et_titre() {
        final ProblemDetail problem = ExceptionHandlerSupport.problemDetail(
                HttpStatus.BAD_REQUEST, "le téléphone est invalide", "Requête invalide");

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getDetail()).isEqualTo("le téléphone est invalide");
        assertThat(problem.getTitle()).isEqualTo("Requête invalide");
    }
}
