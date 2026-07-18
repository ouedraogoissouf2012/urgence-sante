/// Centre recommandé pour le besoin exprimé, avec la raison du classement.
class RecommendedCenter {
  const RecommendedCenter({
    required this.facilityId,
    required this.name,
    required this.distanceMeters,
    required this.status,
    required this.explanation,
    this.travelTimeSeconds,
  });

  final String facilityId;
  final String name;
  final double distanceMeters;

  /// Temps de trajet estimé, absent si indisponible (mode dégradé).
  final double? travelTimeSeconds;

  /// Statut de disponibilité du contrat (ex. « AVAILABLE », « UNKNOWN »).
  final String status;

  /// Raison lisible de la recommandation.
  final String explanation;
}
