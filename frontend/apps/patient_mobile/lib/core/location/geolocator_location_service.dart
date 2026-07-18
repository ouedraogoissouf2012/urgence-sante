import 'package:geolocator/geolocator.dart';

import 'location_service.dart';

/// Implémentation Geolocator : permissions puis position courante.
class GeolocatorLocationService implements LocationService {
  const GeolocatorLocationService();

  @override
  Future<UserPosition> currentPosition() async {
    if (!await Geolocator.isLocationServiceEnabled()) {
      throw const LocationUnavailableException(
        'La localisation est désactivée sur cet appareil.',
      );
    }

    LocationPermission permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
    }
    if (permission == LocationPermission.denied ||
        permission == LocationPermission.deniedForever) {
      throw const LocationUnavailableException(
        "L'autorisation de localisation est nécessaire pour trouver un centre proche.",
      );
    }

    final Position position = await Geolocator.getCurrentPosition();
    return UserPosition(latitude: position.latitude, longitude: position.longitude);
  }
}
