package com.urgencesante.orientation.internal.adapter.in.web;

import com.urgencesante.orientation.internal.adapter.in.web.dto.response.RecommendationResponse;
import com.urgencesante.orientation.internal.adapter.in.web.mapper.OrientationWebMapper;
import com.urgencesante.orientation.internal.application.port.in.RecommendFacilitiesUseCase;
import com.urgencesante.orientation.internal.application.query.OrientationQuery;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptateur entrant REST de l'orientation. Conforme au contrat OpenAPI. Aucune
 * logique métier : validation, mapping et appel du port entrant.
 */
@RestController
@RequestMapping("/api/v1/orientation")
public class OrientationController {

    private static final int DEFAULT_RADIUS_METERS = 15_000;
    private static final int DEFAULT_LIMIT = 5;

    private final RecommendFacilitiesUseCase recommendFacilities;
    private final OrientationWebMapper mapper;

    /**
     * Rayon appliqué quand la requête n'en fournit pas. Vaut 15 km par défaut
     * (produit) mais reste surchargeable — utile en local où l'appareil de test
     * peut être loin du jeu de données d'Abidjan : {@code orientation.default-radius-meters}.
     */
    private final int defaultRadiusMeters;

    public OrientationController(
            RecommendFacilitiesUseCase recommendFacilities,
            OrientationWebMapper mapper,
            @Value("${orientation.default-radius-meters:" + DEFAULT_RADIUS_METERS + "}")
                    int defaultRadiusMeters) {
        // Échec RAPIDE au démarrage : une valeur de configuration hors bornes est
        // une faute serveur, pas une faute client. Sans ce contrôle, elle passerait
        // inaperçue jusqu'à la première requête sans rayon, qui renverrait alors un
        // 400 trompeur (validé côté OrientationQuery) au lieu de bloquer le boot.
        Assert.isTrue(
                defaultRadiusMeters >= 1 && defaultRadiusMeters <= OrientationQuery.MAX_RADIUS_METERS,
                () -> "orientation.default-radius-meters doit être dans [1, "
                        + OrientationQuery.MAX_RADIUS_METERS + "] mètres, reçu : " + defaultRadiusMeters);
        this.recommendFacilities = recommendFacilities;
        this.mapper = mapper;
        this.defaultRadiusMeters = defaultRadiusMeters;
    }

    @GetMapping
    public List<RecommendationResponse> recommend(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(required = false) String service,
            @RequestParam(required = false) List<String> services,
            @RequestParam(required = false) Integer radiusMeters,
            @RequestParam(required = false) Integer limit) {
        // Un symptôme peut couvrir plusieurs services : « services » (répété) est
        // accepté en plus de « service » (mono, rétrocompatible). L'union des deux
        // constitue le besoin ; l'absence totale échoue en 400 (OrientationQuery).
        final Set<String> serviceCodes = new LinkedHashSet<>();
        if (services != null) {
            serviceCodes.addAll(services);
        }
        if (service != null) {
            serviceCodes.add(service);
        }
        final OrientationQuery query = new OrientationQuery(
                lat, lon, serviceCodes,
                radiusMeters == null ? defaultRadiusMeters : radiusMeters,
                limit == null ? DEFAULT_LIMIT : limit);
        return recommendFacilities.recommend(query).stream().map(mapper::toResponse).toList();
    }
}
