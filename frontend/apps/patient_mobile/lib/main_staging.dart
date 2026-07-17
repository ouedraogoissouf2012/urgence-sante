import 'package:app_foundation/app_foundation.dart';

import 'bootstrap.dart';

/// Entrée « staging » : `flutter run -t lib/main_staging.dart`.
Future<void> main() =>
    bootstrap(AppConfig.forEnvironment(AppEnvironment.staging));
