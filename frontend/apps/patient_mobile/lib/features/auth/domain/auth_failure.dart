/// Cause d'échec d'une inscription ou connexion, pour un message adapté.
enum AuthFailure {
  /// Identifiants incorrects (connexion) — HTTP 401.
  invalidCredentials,

  /// Numéro déjà utilisé (inscription) — HTTP 409.
  phoneAlreadyUsed,

  /// Saisie invalide (téléphone mal formé, mot de passe trop court) — HTTP 400.
  invalidInput,

  /// Trop de tentatives — HTTP 429.
  tooManyAttempts,

  /// Réseau indisponible ou erreur serveur.
  network,
}

/// Exception métier d'authentification (portée par le view-model, pas de Flutter).
class AuthException implements Exception {
  const AuthException(this.failure, this.message);

  final AuthFailure failure;
  final String message;
}
