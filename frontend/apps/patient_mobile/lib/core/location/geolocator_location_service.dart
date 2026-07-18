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

    final Position position = await Geolocator.getCurrentPosition();
    return UserPosition(latitude: position.latitude, longitude: position.longitude);
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
