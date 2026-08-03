package com.urgencesante.patient.internal.adapter.in.web.dto.response;

import java.util.UUID;

/** Session ouverte renvoyée au client : identifiant + jeton porteur en clair. */
public record PatientSessionResponse(UUID patientId, String token) {
}
