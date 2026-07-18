import 'package:api_client/api.dart';
import 'package:app_foundation/app_foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../core/calls/emergency_caller.dart';
import '../core/consent/consent_store.dart';
import '../core/consent/shared_prefs_consent_store.dart';
import '../core/location/geolocator_location_service.dart';
import '../core/location/location_service.dart';
import '../core/navigation/navigation_launcher.dart';
import '../core/storage/key_value_store.dart';
import '../features/orientation/data/api_orientation_repository.dart';
import '../features/orientation/data/cached_orientation_repository.dart';
import '../features/orientation/domain/repository/orientation_repository.dart';

/// Configuration d'environnement, fournie au démarrage (bootstrap).
final appConfigProvider = Provider<AppConfig>(
  (ref) => throw UnimplementedError('Fourni par bootstrap via overrides'),
);

/// Client HTTP généré, pointé sur l'API de l'environnement.
final apiClientProvider = Provider<ApiClient>(
  (ref) => ApiClient(basePath: ref.watch(appConfigProvider).apiBaseUrl),
);

/// Stockage local (cache hors ligne, Lot 1).
final keyValueStoreProvider = Provider<KeyValueStore>(
  (ref) => const SharedPrefsKeyValueStore(),
);

/// Accès aux données du parcours d'orientation : adaptateur API décoré du
/// cache hors ligne (réseau d'abord, repli local daté).
final orientationRepositoryProvider = Provider<OrientationRepository>(
  (ref) => CachedOrientationRepository(
    ApiOrientationRepository(ref.watch(apiClientProvider)),
    ref.watch(keyValueStoreProvider),
  ),
);

/// Accès à la position de l'utilisateur.
final locationServiceProvider = Provider<LocationService>(
  (ref) => const GeolocatorLocationService(),
);

/// Déclenchement des appels d'urgence.
final emergencyCallerProvider = Provider<EmergencyCaller>(
  (ref) => const DialerEmergencyCaller(),
);

/// Lancement d'un itinéraire vers un centre (application cartographique).
final navigationLauncherProvider = Provider<NavigationLauncher>(
  (ref) => const ExternalMapNavigationLauncher(),
);

/// Persistance du consentement (version des conditions acceptée).
final consentStoreProvider = Provider<ConsentStore>(
  (ref) => const SharedPrefsConsentStore(),
);

/// Vrai si la version courante des conditions a été acceptée. Invalidé après
/// acceptation pour rafraîchir le portail d'entrée.
final consentUpToDateProvider = FutureProvider<bool>((ref) async {
  final accepted = await ref.watch(consentStoreProvider).acceptedTermsVersion();
  return isConsentUpToDate(accepted);
});
