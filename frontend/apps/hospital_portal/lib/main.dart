import 'package:app_foundation/app_foundation.dart';

import 'bootstrap.dart';

/// Point d'entrée par défaut (environnement de développement).
Future<void> main() =>
    bootstrap(AppConfig.forEnvironment(AppEnvironment.development));
