import 'package:app_foundation/app_foundation.dart';

import 'bootstrap.dart';

/// Entrée « développement » : `flutter run -t lib/main_development.dart`.
Future<void> main() =>
    bootstrap(AppConfig.forEnvironment(AppEnvironment.development));
