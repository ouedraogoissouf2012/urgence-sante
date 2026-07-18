/// Position géographique de l'utilisateur.
class UserPosition {
  const UserPosition({required this.latitude, required this.longitude});

  final double latitude;
  final double longitude;
}

/// Échec d'obtention de la position (permission refusée, service coupé…).
class LocationUnavailableException implements Exception {
  const LocationUnavailableException(this.message);

  final String message;

  @override
  String toString() => message;
}

/// Contrat d'accès à la position. Implémenté par Geolocator en production,
/// substituable par un faux en test.
abstract interface class LocationService {
  Future<UserPosition> currentPosition();
}
