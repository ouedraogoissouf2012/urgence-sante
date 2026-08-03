package com.urgencesante.patient.internal.application.result;

import java.util.UUID;

/**
 * Session ouverte : le jeton porteur EN CLAIR (à remettre au client une seule
 * fois) et l'identifiant du patient. Le jeton n'est jamais re-consultable.
 */
public record PatientSession(UUID patientId, String token) {
}
