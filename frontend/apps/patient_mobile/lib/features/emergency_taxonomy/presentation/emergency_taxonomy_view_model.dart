import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../di/providers.dart';
import '../domain/model/emergency_category.dart';
import 'emergency_taxonomy_state.dart';

/// ViewModel du sélecteur d'urgence à deux niveaux (catégorie → symptôme).
final emergencyTaxonomyViewModelProvider =
    NotifierProvider<EmergencyTaxonomyViewModel, EmergencyTaxonomyState>(
  EmergencyTaxonomyViewModel.new,
);

/// Charge la taxonomie et gère la navigation à deux niveaux. Aucune dépendance
/// widget : testable en pur Dart. Le déclenchement de la recherche ou de l'appel
/// direct est décidé par la View (qui dispose du contexte de navigation).
class EmergencyTaxonomyViewModel extends Notifier<EmergencyTaxonomyState> {
  @override
  EmergencyTaxonomyState build() {
    Future.microtask(load);
    return const EmergencyTaxonomyState();
  }

  Future<void> load() async {
    state = state.copyWith(phase: EmergencyTaxonomyPhase.loading, clearError: true);
    try {
      final categories =
          await ref.read(emergencyTaxonomyRepositoryProvider).fetchTaxonomy();
      if (categories.isEmpty) {
        // Défense en profondeur : une taxonomie vide ne doit JAMAIS aboutir à un
        // écran d'urgence blanc. La couche données lève déjà sur une réponse
        // distante vide ; cette garde assure qu'aucun chemin ne laisse un
        // « ready » sans catégorie (l'écran affiche une erreur avec réessai).
        state = state.copyWith(
          phase: EmergencyTaxonomyPhase.error,
          errorMessage:
              'Impossible de charger les urgences. Vérifiez votre connexion.',
        );
        return;
      }
      state = state.copyWith(
        phase: EmergencyTaxonomyPhase.ready,
        categories: categories,
        clearError: true,
      );
    } on Exception {
      state = state.copyWith(
        phase: EmergencyTaxonomyPhase.error,
        errorMessage:
            'Impossible de charger les urgences. Vérifiez votre connexion.',
      );
    }
  }

  /// Ouvre une catégorie : passe au niveau 2 (ses symptômes).
  void openCategory(EmergencyCategory category) {
    state = state.copyWith(selectedCategory: category);
  }

  /// Revient au niveau 1 (liste des catégories).
  void back() {
    state = state.copyWith(clearSelectedCategory: true);
  }

  Future<void> retry() => load();
}
