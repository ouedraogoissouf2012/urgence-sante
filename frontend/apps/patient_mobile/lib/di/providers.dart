import 'package:api_client/api.dart';
import 'package:app_foundation/app_foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../core/calls/emergency_caller.dart';
import '../core/location/geolocator_location_service.dart';
import '../core/location/location_service.dart';
import '../features/orientation/data/api_orientation_repository.dart';
import '../features/orientation/domain/repository/orientation_repository.dart';

/// Configuration d'environnement, fournie au démarrage (bootstrap).
final appConfigProvider = Provider<AppConfig>(
  (ref) => throw UnimplementedError('Fourni par bootstrap via overrides'),
);

/// Client HTTP généré, pointé sur l'API de l'environnement.
final apiClientProvider = Provider<ApiClient>(
  (ref) => ApiClient(basePath: ref.watch(appConfigProvider).apiBaseUrl),
);

/// Accès aux données du parcours d'orientation.
final orientationRepositoryProvider = Provider<OrientationRepository>(
  (ref) => ApiOrientationRepository(ref.watch(apiClientProvider)),
);

/// Accès à la position de l'utilisateur.
final locationServiceProvider = Provider<LocationService>(
  (ref) => const GeolocatorLocationService(),
);

/// Déclenchement des appels d'urgence.
final emergencyCallerProvider = Provider<EmergencyCaller>(
  (ref) => const DialerEmergencyCaller(),
);
