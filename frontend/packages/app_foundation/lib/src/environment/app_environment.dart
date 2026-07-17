/// Environnements d'exécution de l'application.
///
/// Sélectionné au démarrage par le point d'entrée correspondant
/// (`main_development.dart`, `main_staging.dart`, `main_production.dart`).
enum AppEnvironment {
  development,
  staging,
  production;

  /// Vrai pour l'environnement de production uniquement.
  bool get isProduction => this == AppEnvironment.production;
}
