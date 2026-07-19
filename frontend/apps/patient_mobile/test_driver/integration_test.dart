import 'package:integration_test/integration_test_driver.dart';

/// Pilote des tests d'intégration navigateur (flutter drive).
///
/// Exécution (depuis apps/patient_mobile, chromedriver actif sur :4444) :
///   flutter drive --driver=test_driver/integration_test.dart \
///     --target=integration_test/parcours_e2e_test.dart \
///     -d web-server --browser-name=chrome --headless
Future<void> main() => integrationDriver();
