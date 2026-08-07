import '../model/emergency_category.dart';

/// Contrat de données de la taxonomie des urgences (référentiel de navigation à
/// deux niveaux). Implémenté par l'adaptateur API ; substituable par un faux en
/// test.
abstract interface class EmergencyTaxonomyRepository {
  /// Catégories d'urgence (avec symptômes et services recherchés), ordonnées
  /// pour l'affichage.
  Future<List<EmergencyCategory>> fetchTaxonomy();
}
