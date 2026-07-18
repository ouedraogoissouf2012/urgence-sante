import '../model/cached.dart';
import '../model/medical_need.dart';
import '../model/recommended_center.dart';

/// Contrat de données du parcours d'orientation.
///
/// Implémenté par l'adaptateur API (décoré d'un cache local pour le mode hors
/// ligne du Lot 1) ; substituable par un faux en test.
abstract interface class OrientationRepository {
  /// Catalogue des besoins médicaux. En panne réseau, la dernière version
  /// synchronisée est servie depuis le cache (provenance et date exposées).
  Future<Cached<List<MedicalNeed>>> loadNeeds();

  /// Centres recommandés pour un besoin depuis une position (réseau requis :
  /// le classement temps réel n'est jamais simulé hors ligne).
  Future<List<RecommendedCenter>> recommend({
    required double latitude,
    required double longitude,
    required String serviceCode,
  });

  /// Derniers centres connus (annuaire minimal hors ligne), ou `null` si
  /// aucune recherche n'a encore été synchronisée.
  Future<Cached<List<RecommendedCenter>>?> lastKnownCenters();
}
