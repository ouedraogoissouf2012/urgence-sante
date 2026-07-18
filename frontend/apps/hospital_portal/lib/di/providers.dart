import 'package:api_client/api.dart';
import 'package:app_foundation/app_foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../features/board/data/api_portal_repository.dart';
import '../features/board/domain/repository/portal_repository.dart';

/// Configuration d'environnement, fournie au démarrage (bootstrap).
final appConfigProvider = Provider<AppConfig>(
  (ref) => throw UnimplementedError('Fourni par bootstrap via overrides'),
);

/// Client HTTP généré, pointé sur l'API de l'environnement.
final apiClientProvider = Provider<ApiClient>(
  (ref) => ApiClient(basePath: ref.watch(appConfigProvider).apiBaseUrl),
);

/// Accès aux données du portail.
final portalRepositoryProvider = Provider<PortalRepository>(
  (ref) => ApiPortalRepository(ref.watch(apiClientProvider)),
);
