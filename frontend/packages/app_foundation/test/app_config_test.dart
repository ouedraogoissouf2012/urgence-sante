import 'package:app_foundation/app_foundation.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  group('AppConfig.forEnvironment', () {
    test('fournit une URL de développement locale', () {
      final config = AppConfig.forEnvironment(AppEnvironment.development);

      expect(config.environment, AppEnvironment.development);
      expect(config.apiBaseUrl, contains('localhost'));
      expect(config.environment.isProduction, isFalse);
    });

    test('marque la production comme telle', () {
      final config = AppConfig.forEnvironment(AppEnvironment.production);

      expect(config.environment.isProduction, isTrue);
      expect(config.apiBaseUrl, startsWith('https://'));
    });
  });
}
