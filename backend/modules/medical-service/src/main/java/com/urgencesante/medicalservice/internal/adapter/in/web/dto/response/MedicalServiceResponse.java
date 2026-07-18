package com.urgencesante.medicalservice.internal.adapter.in.web.dto.response;

/** Service médical exposé par l'API (conforme au schéma OpenAPI MedicalService). */
public record MedicalServiceResponse(String code, String label, String category) {
}
