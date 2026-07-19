package com.urgencesante.availability.internal.application.port.out;

import java.util.function.Supplier;

/**
 * Port sortant : frontière transactionnelle pilotée par le CAS D'USAGE
 * (ARCHITECTURE.md), sans dépendance Spring dans l'application. L'adaptateur
 * délègue au gestionnaire de transactions de la plateforme.
 */
public interface TransactionPort {

    /** Exécute le travail dans une transaction (commit au retour, rollback sur exception). */
    <T> T inTransaction(Supplier<T> work);
}
