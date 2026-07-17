import 'app_environment.dart';

/// Configuration immuable dérivée de l'environnement d'exécution.
///
/// Les valeurs par défaut sont fournies par [AppConfig.forEnvironment]. Aucune
/// donnée sensible n'est stockée ici : seules des URL publiques de service.
class AppConfig {
  const AppConfig({
    required this.environment,
    required this.apiBaseUrl,
  });

  /// Configuration par défaut pour un environnement donné.
  factory AppConfig.forEnvironment(AppEnvironment environment) {
    return switch (environment) {
      AppEnvironment.development => const AppConfig(
          environment: AppEnvironment.development,
          apiBaseUrl: 'http://localhost:8080/api/v1',
        ),
      AppEnvironment.staging => const AppConfig(
          environment: AppEnvironment.staging,
          apiBaseUrl: 'https://staging.urgence-sante.ci/api/v1',
        ),
      AppEnvironment.production => const AppConfig(
          environment: AppEnvironment.production,
          apiBaseUrl: 'https://urgence-sante.ci/api/v1',
        ),
    };
  }

  final AppEnvironment environment;
  final String apiBaseUrl;
}
