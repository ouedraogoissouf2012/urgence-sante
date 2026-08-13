import 'dart:async';

import 'package:geolocator/geolocator.dart';
import 'package:meta/meta.dart';

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
    } on Exception catch (exception) {
      throw mapPositionException(exception);
    }
  }

  /// Qualifie un échec de {@link Geolocator.getCurrentPosition} : un timeout,
  /// une panne inconnue ou une désactivation/refus survenu pendant l'appel ne
  /// sont pas la même chose qu'un refus de permission initial (message et
  /// action différents côté interface — cf. issue #140).
  @visibleForTesting
  static LocationUnavailableException mapPositionException(Object exception) {
    if (exception is TimeoutException) {
      return const LocationUnavailableException(
        'La localisation prend trop de temps. Réessayez, ou continuez sans '
        'position précise.',
        LocationFailure.timeout,
      );
    }
    if (exception is LocationServiceDisabledException) {
      return const LocationUnavailableException(
        'La localisation a été désactivée pendant la recherche.',
        LocationFailure.serviceDisabled,
      );
    }
    if (exception is PermissionDeniedException) {
      return const LocationUnavailableException(
        "L'autorisation de localisation est nécessaire pour trouver un centre proche.",
        LocationFailure.denied,
      );
    }
    return const LocationUnavailableException(
      "Impossible d'obtenir votre position. Réessayez.",
      LocationFailure.unknown,
    );
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
