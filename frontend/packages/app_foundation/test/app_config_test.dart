import 'package:app_foundation/app_foundation.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  group('AppConfig.forEnvironment', () {
    test('fournit une URL de développement locale (port démo 8090)', () {
      final config = AppConfig.forEnvironment(AppEnvironment.development);

      expect(config.environment, AppEnvironment.development);
      expect(config.apiBaseUrl, 'http://localhost:8090/api/v1');
      expect(config.environment.isProduction, isFalse);
    });

    test('marque la production comme telle', () {
      final config = AppConfig.forEnvironment(AppEnvironment.production);

      expect(config.environment.isProduction, isTrue);
      expect(config.apiBaseUrl, startsWith('https://'));
    });
  });

  group('AppConfig.resolveApiBaseUrl', () {
    test('la surcharge non vide prime sur le défaut', () {
      expect(
        AppConfig.resolveApiBaseUrl(
          AppEnvironment.development,
          override: 'http://10.0.2.2:8090/api/v1',
        ),
        'http://10.0.2.2:8090/api/v1',
      );
    });

    test('une surcharge vide ou blanche est ignorée', () {
      expect(
        AppConfig.resolveApiBaseUrl(AppEnvironment.development, override: '  '),
        'http://localhost:8090/api/v1',
      );
      expect(
        AppConfig.resolveApiBaseUrl(AppEnvironment.development, override: null),
        'http://localhost:8090/api/v1',
      );
    });

    test('la surcharge est nettoyée des espaces', () {
      expect(
        AppConfig.resolveApiBaseUrl(
          AppEnvironment.production,
          override: ' http://192.168.1.20:8090/api/v1 ',
        ),
        'http://192.168.1.20:8090/api/v1',
      );
    });
  });
}
