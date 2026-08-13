package com.urgencesante.identity.internal.application.command;

import com.urgencesante.identity.PortalRole;
import java.util.UUID;

/** Demande de création d'un credential portail (label, rôle, établissement le cas échéant). */
public record ProvisionCredentialCommand(String label, PortalRole role, UUID facilityId) {
}
