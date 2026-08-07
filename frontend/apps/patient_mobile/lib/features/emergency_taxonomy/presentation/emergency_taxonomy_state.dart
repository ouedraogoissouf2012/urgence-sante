import '../domain/model/emergency_category.dart';

/// Phase du chargement de la taxonomie.
enum EmergencyTaxonomyPhase { loading, ready, error }

/// État immuable du sélecteur d'urgence à deux niveaux (flux unidirectionnel).
class EmergencyTaxonomyState {
  const EmergencyTaxonomyState({
    this.phase = EmergencyTaxonomyPhase.loading,
    this.categories = const [],
    this.selectedCategory,
    this.errorMessage,
  });

  final EmergencyTaxonomyPhase phase;
  final List<EmergencyCategory> categories;

  /// Catégorie ouverte (niveau 2 = ses symptômes) ; `null` au niveau 1.
  final EmergencyCategory? selectedCategory;

  final String? errorMessage;

  /// Vrai si l'on affiche les symptômes d'une catégorie (second niveau).
  bool get isLevelTwo => selectedCategory != null;

  EmergencyTaxonomyState copyWith({
    EmergencyTaxonomyPhase? phase,
    List<EmergencyCategory>? categories,
    EmergencyCategory? selectedCategory,
    String? errorMessage,
    bool clearSelectedCategory = false,
    bool clearError = false,
  }) {
    return EmergencyTaxonomyState(
      phase: phase ?? this.phase,
      categories: categories ?? this.categories,
      selectedCategory: clearSelectedCategory
          ? null
          : (selectedCategory ?? this.selectedCategory),
      errorMessage: clearError ? null : (errorMessage ?? this.errorMessage),
    );
  }
}
