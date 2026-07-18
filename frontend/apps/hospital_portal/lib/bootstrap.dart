import 'package:app_foundation/app_foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'app/portal_app.dart';
import 'di/providers.dart';

/// Démarrage commun à tous les environnements du portail hospitalier.
Future<void> bootstrap(AppConfig config) async {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(
    ProviderScope(
      overrides: [appConfigProvider.overrideWithValue(config)],
      child: const PortalApp(),
    ),
  );
}
