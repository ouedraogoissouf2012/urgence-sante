import 'package:app_foundation/app_foundation.dart';

import 'bootstrap.dart';

/// Entrée « production » : `flutter run -t lib/main_production.dart`.
Future<void> main() =>
    bootstrap(AppConfig.forEnvironment(AppEnvironment.production));
