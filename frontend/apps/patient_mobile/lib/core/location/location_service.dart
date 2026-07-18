/// Position géographique de l'utilisateur.
class UserPosition {
  const UserPosition({required this.latitude, required this.longitude});

  final double latitude;
  final double longitude;
}

/// Cause d'indisponibilité de la position, pour proposer l'action adaptée.
enum LocationFailure {
  /// Service de localisation désactivé sur l'appareil.
  serviceDisabled,

  /// Autorisation refusée (redemandable).
  denied,

  /// Autorisation refusée définitivement (passer par les réglages).
  deniedForever,
}

/// Échec d'obtention de la position, qualifié pour l'interface.
class LocationUnavailableException implements Exception {
  const LocationUnavailableException(this.message, this.failure);

  final String message;
  final LocationFailure failure;

  /// L'action utile passe par les réglages (système ou application).
  bool get needsSettings =>
      failure == LocationFailure.deniedForever ||
      failure == LocationFailure.serviceDisabled;

  @override
  String toString() => message;
}

/// Contrat d'accès à la position. Implémenté par Geolocator en production,
/// substituable par un faux en test.
abstract interface class LocationService {
  Future<UserPosition> currentPosition();

  /// Ouvre les réglages pertinents (application ou localisation système).
  Future<void> openSettings(LocationFailure failure);
}
