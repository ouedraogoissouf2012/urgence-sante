package com.urgencesante.identity;

/** Rôle d'un porteur de jeton du portail. */
public enum PortalRole {
    /** Opérateur d'un établissement : n'agit que sur SON établissement. */
    FACILITY_OPERATOR,

    /** Administrateur (ex. régulation SAMU) : agit sur tout établissement. */
    ADMIN
}
