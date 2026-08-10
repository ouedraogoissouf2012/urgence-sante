import 'package:api_client/api.dart';
import 'package:app_foundation/app_foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../core/auth/secure_session_store.dart';
import '../core/auth/session_store.dart';
import '../core/calls/emergency_caller.dart';
import '../core/consent/consent_store.dart';
import '../core/consent/shared_prefs_consent_store.dart';
import '../core/location/geolocator_location_service.dart';
import '../core/location/location_service.dart';
import '../core/navigation/navigation_launcher.dart';
import '../core/storage/key_value_store.dart';
import '../features/auth/data/api_auth_repository.dart';
import '../features/auth/domain/auth_repository.dart';
import '../features/emergency_taxonomy/data/api_emergency_taxonomy_repository.dart';
import '../features/emergency_taxonomy/data/cached_emergency_taxonomy_repository.dart';
import '../features/emergency_taxonomy/domain/repository/emergency_taxonomy_repository.dart';
import '../features/medical_profile/data/local_medical_profile_store.dart';
import '../features/medical_profile/domain/profile_store.dart';
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

/// Stockage local EN CLAIR (cache hors ligne, Lot 1 — jamais pour une donnée
/// sensible : voir [secureKeyValueStoreProvider]).
final keyValueStoreProvider = Provider<KeyValueStore>(
  (ref) => const SharedPrefsKeyValueStore(),
);

/// Stockage local CHIFFRÉ (Keystore/Keychain), pour toute donnée sensible
/// (santé, session).
final secureKeyValueStoreProvider = Provider<KeyValueStore>(
  (ref) => SecureFlutterKeyValueStore(),
);

/// Accès aux données du parcours d'orientation : adaptateur API décoré du
/// cache hors ligne (réseau d'abord, repli local daté).
final orientationRepositoryProvider = Provider<OrientationRepository>(
  (ref) => CachedOrientationRepository(
    ApiOrientationRepository(ref.watch(apiClientProvider)),
    ref.watch(keyValueStoreProvider),
  ),
);

/// Accès à la taxonomie des urgences (référentiel de navigation à 2 niveaux) :
/// adaptateur API décoré d'un cache hors ligne (réseau d'abord, repli local).
final emergencyTaxonomyRepositoryProvider = Provider<EmergencyTaxonomyRepository>(
  (ref) => CachedEmergencyTaxonomyRepository(
    ApiEmergencyTaxonomyRepository(ref.watch(apiClientProvider)),
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

// ── Authentification patient ────────────────────────────────────────────────

/// Stockage CHIFFRÉ du jeton de session (Keystore/Keychain).
final sessionStoreProvider = Provider<SessionStore>(
  (ref) => SecureSessionStore(),
);

/// Accès à l'authentification patient (inscription/connexion via l'API).
final authRepositoryProvider = Provider<AuthRepository>(
  (ref) => ApiAuthRepository(ref.watch(apiClientProvider)),
);

/// Jeton de session courant (null si non connecté). Invalidé après
/// inscription/connexion/déconnexion pour rafraîchir la porte d'entrée.
final sessionTokenProvider = FutureProvider<String?>(
  (ref) => ref.watch(sessionStoreProvider).readToken(),
);

/// Persistance LOCALE et CHIFFRÉE de la fiche médicale (jamais côté serveur).
/// Reprend automatiquement (migration silencieuse) une fiche enregistrée en
/// clair par une version antérieure au correctif #128.
final profileStoreProvider = Provider<ProfileStore>(
  (ref) => LocalMedicalProfileStore(
    ref.watch(secureKeyValueStoreProvider),
    ref.watch(keyValueStoreProvider),
  ),
);

/// Mode invité : l'utilisateur a choisi « Urgence — continuer sans compte ».
/// Volatil (non persisté) : l'inscription est reproposée au prochain lancement,
/// mais une urgence en cours n'est jamais interrompue.
final guestModeProvider =
    NotifierProvider<GuestModeNotifier, bool>(GuestModeNotifier.new);

class GuestModeNotifier extends Notifier<bool> {
  @override
  bool build() => false;

  /// Bascule en mode invité (issue d'urgence, sans compte).
  void enter() => state = true;
}
