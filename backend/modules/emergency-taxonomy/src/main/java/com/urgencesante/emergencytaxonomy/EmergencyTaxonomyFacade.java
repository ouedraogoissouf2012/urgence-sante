package com.urgencesante.emergencytaxonomy;

import java.util.List;

/**
 * API publique du module Emergency Taxonomy. Permet aux autres modules et aux
 * tests de cohérence de consulter la taxonomie sans accéder à son interne.
 */
public interface EmergencyTaxonomyFacade {

    /** Catégories d'urgence, ordonnées pour l'affichage. */
    List<EmergencyCategoryView> categories();
}
