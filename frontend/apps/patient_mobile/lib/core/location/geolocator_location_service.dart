import 'package:geolocator/geolocator.dart';

import 'location_service.dart';

/// Implémentation Geolocator : permissions puis position courante, avec une
/// cause d'échec qualifiée pour proposer l'action adaptée.
class GeolocatorLocationService implements LocationService {
  const GeolocatorLocationService();

  @override
  Future<UserPosition> currentPosition() async {
    if (!await Geolocator.isLocationServiceEnabled()) {
      throw const LocationUnavailableException(
        'La localisation est désactivée sur cet appareil.',
        LocationFailure.serviceDisabled,
      );
    }

    LocationPermission permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
    }
    if (permission == LocationPermission.deniedForever) {
      throw const LocationUnavailableException(
        "L'autorisation de localisation a été refusée définitivement : "
        "activez-la dans les réglages de l'application.",
        LocationFailure.deniedForever,
      );
    }
    if (permission == LocationPermission.denied) {
      throw const LocationUnavailableException(
        "L'autorisation de localisation est nécessaire pour trouver un centre proche.",
        LocationFailure.denied,
      );
    }

    // Délai borné : sans réponse (permission jamais accordée, GPS lent), on
    // échoue proprement au lieu de laisser l'utilisateur bloqué sur « Recherche… ».
    // Le parcours dégradé (« Continuer sans position précise ») prend le relais.
    try {
      final Position position = await Geolocator.getCurrentPosition(
        locationSettings: const LocationSettings(timeLimit: Duration(seconds: 12)),
      );
      return UserPosition(latitude: position.latitude, longitude: position.longitude);
    } on Exception {
      throw const LocationUnavailableException(
        "Impossible d'obtenir votre position à temps.",
        LocationFailure.denied,
      );
    }
  }

  @override
  Future<void> openSettings(LocationFailure failure) async {
    if (failure == LocationFailure.serviceDisabled) {
      await Geolocator.openLocationSettings();
    } else {
      await Geolocator.openAppSettings();
    }
  }
}
