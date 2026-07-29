package com.urgencesante.patient.internal.application.command;

/** Données d'inscription telles que soumises (téléphone et mot de passe bruts). */
public record RegisterPatientCommand(String phone, String password) {
}
