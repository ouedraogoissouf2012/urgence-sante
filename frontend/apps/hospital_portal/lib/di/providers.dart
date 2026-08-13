import 'package:api_client/api.dart';
import 'package:app_foundation/app_foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../features/board/data/api_portal_repository.dart';
import '../features/board/domain/repository/portal_repository.dart';

/// Configuration d'environnement, fournie au démarrage (bootstrap).
final appConfigProvider = Provider<AppConfig>(
  (ref) => throw UnimplementedError('Fourni par bootstrap via overrides'),
);

/// Stockage CHIFFRÉ du jeton opérateur (Keystore/Keychain), clé dédiée au
/// portail pour ne jamais collisionner avec un autre jeton sur le même
/// appareil (factorisé dans app_foundation — voir issue #163).
final tokenStoreProvider = Provider<TokenStore>(
  (ref) => SecureTokenStore('hospital_portal_operator_token'),
);

/// Jeton opérateur actuellement stocké, `null` si aucun. Ré-évalué à chaque
/// `invalidate` (connexion, déconnexion, jeton rejeté par le serveur).
final sessionTokenProvider = FutureProvider<String?>(
  (ref) => ref.watch(tokenStoreProvider).readToken(),
);

/// Client HTTP généré, pointé sur l'API de l'environnement et porteur du
/// jeton opérateur courant. Reconstruit automatiquement quand
/// [sessionTokenProvider] change (connexion/déconnexion) : tout consommateur
/// qui dépend de ce provider reçoit toujours un client à jour, sans câblage
/// manuel.
final apiClientProvider = Provider<ApiClient>((ref) {
  final String? token = ref.watch(sessionTokenProvider).value;
  final ApiClient client = ApiClient(
    basePath: ref.watch(appConfigProvider).apiBaseUrl,
    authentication: (token == null || token.isEmpty) ? null : (HttpBearerAuth()..accessToken = token),
  );
  // Un nouveau client (donc une nouvelle connexion HTTP sous-jacente) est créé
  // à chaque connexion/déconnexion : ferme l'ancien plutôt que de le laisser
  // ouvert pour le reste du processus.
  ref.onDispose(client.client.close);
  return client;
});

/// Accès aux données du portail.
final portalRepositoryProvider = Provider<PortalRepository>(
  (ref) => ApiPortalRepository(ref.watch(apiClientProvider)),
);
