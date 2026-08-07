import '../model/cached.dart';
import '../model/recommended_center.dart';

/// Contrat de données du parcours d'orientation.
///
/// Implémenté par l'adaptateur API (décoré d'un cache local pour le mode hors
/// ligne du Lot 1) ; substituable par un faux en test.
abstract interface class OrientationRepository {
  /// Centres recommandés pour un besoin depuis une position (réseau requis :
  /// le classement temps réel n'est jamais simulé hors ligne). Accepte
  /// plusieurs services (sémantique OU) : un symptôme peut couvrir plusieurs
  /// spécialités.
  Future<List<RecommendedCenter>> recommend({
    required double latitude,
    required double longitude,
    required List<String> serviceCodes,
  });

  /// Derniers centres connus POUR CE BESOIN (annuaire minimal hors ligne), ou
  /// `null` si aucune recherche du même besoin n'a encore été synchronisée. Le
  /// repli est cloisonné par besoin : hors ligne, on ne sert jamais les centres
  /// d'une autre urgence — ils ne répondent pas au besoin courant et seraient
  /// trompeurs.
  Future<Cached<List<RecommendedCenter>>?> lastKnownCenters({
    required List<String> serviceCodes,
  });
}
