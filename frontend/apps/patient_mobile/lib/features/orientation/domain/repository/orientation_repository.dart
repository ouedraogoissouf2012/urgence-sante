import '../model/medical_need.dart';
import '../model/recommended_center.dart';

/// Contrat de données du parcours d'orientation.
///
/// Implémenté par l'adaptateur API ; substituable par un faux en test.
abstract interface class OrientationRepository {
  /// Catalogue des besoins médicaux sélectionnables.
  Future<List<MedicalNeed>> loadNeeds();

  /// Centres recommandés pour un besoin depuis une position.
  Future<List<RecommendedCenter>> recommend({
    required double latitude,
    required double longitude,
    required String serviceCode,
  });
}
