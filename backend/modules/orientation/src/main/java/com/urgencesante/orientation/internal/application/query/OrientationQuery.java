package com.urgencesante.orientation.internal.application.query;

import com.urgencesante.orientation.internal.domain.exception.OrientationValidationException;
import java.util.Objects;
import java.util.Set;

/**
 * Critères d'orientation : position du patient, besoins, rayon et nombre max.
 *
 * <p>{@code serviceCodes} porte un ou plusieurs services (un symptôme peut
 * couvrir plusieurs spécialités) ; un centre offrant AU MOINS UN d'entre eux est
 * candidat. Les maximums sont imposés CÔTÉ SERVEUR, indépendamment du client, et
 * alignés sur le contrat OpenAPI. {@code MAX_LIMIT} borne aussi le nombre
 * d'appels externes (un appel d'itinéraire par candidat retenu).
 */
public record OrientationQuery(
        double latitude, double longitude, Set<String> serviceCodes, int radiusMeters, int limit) {

    /** Rayon maximal de recherche (aligné sur le contrat). */
    public static final int MAX_RADIUS_METERS = 100_000;

    /** Nombre maximal de recommandations (borne les appels externes). */
    public static final int MAX_LIMIT = 20;

    /** Longueur maximale d'un code de service (alignée sur le contrat). */
    public static final int MAX_SERVICE_CODE_LENGTH = 64;

    /** Nombre maximal de services demandés (borne l'expansion de la requête). */
    public static final int MAX_SERVICE_CODES = 16;

    public OrientationQuery {
        Objects.requireNonNull(serviceCodes, "Les services sont requis");
        if (serviceCodes.isEmpty()) {
            throw new OrientationValidationException("Au moins un service est requis");
        }
        if (serviceCodes.size() > MAX_SERVICE_CODES) {
            throw new OrientationValidationException(
                    "Trop de services demandés (max " + MAX_SERVICE_CODES + ")");
        }
        for (final String code : serviceCodes) {
            if (code == null || code.isBlank() || code.length() > MAX_SERVICE_CODE_LENGTH) {
                throw new OrientationValidationException(
                        "Code de service invalide (vide ou > " + MAX_SERVICE_CODE_LENGTH + " caractères)");
            }
        }
        serviceCodes = Set.copyOf(serviceCodes);
        if (!Double.isFinite(latitude) || !Double.isFinite(longitude)) {
            throw new OrientationValidationException(
                    "Coordonnées non finies (NaN/Infini) refusées");
        }
        if (latitude < -90.0 || latitude > 90.0) {
            throw new OrientationValidationException("Latitude hors bornes [-90, 90] : " + latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new OrientationValidationException("Longitude hors bornes [-180, 180] : " + longitude);
        }
        if (radiusMeters < 1 || radiusMeters > MAX_RADIUS_METERS) {
            throw new OrientationValidationException(
                    "Le rayon doit être dans [1, " + MAX_RADIUS_METERS + "] mètres");
        }
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new OrientationValidationException(
                    "La limite doit être dans [1, " + MAX_LIMIT + "]");
        }
    }
}
