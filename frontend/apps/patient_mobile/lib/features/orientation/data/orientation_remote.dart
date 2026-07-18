import '../domain/model/medical_need.dart';
import '../domain/model/recommended_center.dart';

/// Source distante du parcours d'orientation (couche data). Implémentée par
/// l'adaptateur API ; substituable par un faux dans les tests du décorateur
/// de cache.
abstract interface class OrientationRemote {
  Future<List<MedicalNeed>> loadNeeds();

  Future<List<RecommendedCenter>> recommend({
    required double latitude,
    required double longitude,
    required String serviceCode,
  });
}
