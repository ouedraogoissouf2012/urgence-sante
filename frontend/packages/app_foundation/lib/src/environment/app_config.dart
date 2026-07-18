import 'app_environment.dart';

/// Configuration immuable dérivée de l'environnement d'exécution.
///
/// L'URL d'API est surchargée au build via
/// `--dart-define=API_BASE_URL=…` sans modifier le code. Valeurs usuelles :
/// - Windows / Flutter Web : `http://localhost:8090/api/v1` (défaut dev) ;
/// - Android Emulator : `http://10.0.2.2:8090/api/v1` ;
/// - appareil physique : `http://<IP-du-poste>:8090/api/v1`.
class AppConfig {
  const AppConfig({
    required this.environment,
    required this.apiBaseUrl,
  });

  /// Configuration pour un environnement, en respectant une éventuelle
  /// surcharge `API_BASE_URL` fournie au build.
  factory AppConfig.forEnvironment(AppEnvironment environment) {
    return AppConfig(
      environment: environment,
      apiBaseUrl: resolveApiBaseUrl(environment, override: _apiBaseUrlOverride),
    );
  }

  /// Valeur injectée au build (vide si non fournie).
  static const String _apiBaseUrlOverride = String.fromEnvironment('API_BASE_URL');

  /// Résout l'URL d'API : la surcharge non vide prime, sinon le défaut de
  /// l'environnement. Séparée pour être testable (la constante de build ne
  /// l'est pas).
  static String resolveApiBaseUrl(AppEnvironment environment, {String? override}) {
    if (override != null && override.trim().isNotEmpty) {
      return override.trim();
    }
    return switch (environment) {
      // Aligné sur le port du backend de démonstration (scripts/demo-up.sh).
      AppEnvironment.development => 'http://localhost:8090/api/v1',
      AppEnvironment.staging => 'https://staging.urgence-sante.ci/api/v1',
      AppEnvironment.production => 'https://urgence-sante.ci/api/v1',
    };
  }

  final AppEnvironment environment;
  final String apiBaseUrl;
}
