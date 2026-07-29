package com.urgencesante.patient.internal.adapter.in.web.dto.request;

/** Corps de la requête de connexion (conforme au contrat OpenAPI). */
public record LoginRequest(String phone, String password) {
}
