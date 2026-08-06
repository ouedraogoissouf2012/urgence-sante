/**
 * Module Emergency Taxonomy — Taxonomie des urgences médicales.
 *
 * <p>Référentiel de navigation à deux niveaux (grande catégorie → symptôme) issu
 * du document client « Pour les urgences médicales ». Chaque catégorie pointe
 * vers les services médicaux « recherchés » (codes du module Medical Service) et
 * indique si elle relève d'un appel direct des secours.
 *
 * <p>Ce package constitue l'API publique du module. L'implémentation vit dans le
 * sous-package {@code internal} (domaine, application, adaptateurs) et n'est pas
 * accessible aux autres modules. Un module tiers ne dépend que de cette API.
 */
package com.urgencesante.emergencytaxonomy;
