/// Centre recommandé pour le besoin exprimé, avec la raison du classement et
/// les données de fiche (position, téléphone).
class RecommendedCenter {
  const RecommendedCenter({
    required this.facilityId,
    required this.name,
    required this.latitude,
    required this.longitude,
    required this.distanceMeters,
    required this.status,
    required this.explanation,
    this.phone,
    this.travelTimeSeconds,
  });

  final String facilityId;
  final String name;
  final double latitude;
  final double longitude;

  /// Téléphone du centre, absent si inconnu (l'action d'appel est masquée).
  final String? phone;
  final double distanceMeters;

  /// Temps de trajet estimé, absent si indisponible (mode dégradé).
  final double? travelTimeSeconds;

  /// Statut de disponibilité du contrat (ex. « AVAILABLE », « UNKNOWN »).
  final String status;

  /// Raison lisible de la recommandation.
  final String explanation;
}
