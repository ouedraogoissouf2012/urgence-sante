package com.urgencesante.patient.internal.adapter.in.web.dto.request;

/** Corps de la requête d'inscription (conforme au contrat OpenAPI). */
public record RegisterPatientRequest(String phone, String password) {
}
