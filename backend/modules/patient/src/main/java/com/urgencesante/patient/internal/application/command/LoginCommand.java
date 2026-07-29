package com.urgencesante.patient.internal.application.command;

/** Identifiants de connexion (téléphone et mot de passe bruts). */
public record LoginCommand(String phone, String password) {
}
