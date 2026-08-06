package com.urgencesante.emergencytaxonomy.internal.application.port.out;

import com.urgencesante.emergencytaxonomy.internal.domain.model.EmergencyCategory;
import java.util.List;

/**
 * Port sortant : source de la taxonomie. Substituable (LSP) — l'implémentation
 * par défaut fournit le référentiel statique du document client ; un test peut
 * fournir une source en mémoire.
 */
public interface LoadEmergencyTaxonomyPort {

    List<EmergencyCategory> loadAll();
}
