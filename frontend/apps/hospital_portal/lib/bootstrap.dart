import 'package:app_foundation/app_foundation.dart';
import 'package:flutter/widgets.dart';

import 'app/portal_app.dart';

/// Démarrage commun à tous les environnements du portail hospitalier.
Future<void> bootstrap(AppConfig config) async {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(PortalApp(config: config));
}
