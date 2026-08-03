/// Phase du formulaire d'authentification.
enum AuthPhase {
  /// Saisie en cours.
  idle,

  /// Requête en cours (bouton désactivé).
  submitting,

  /// Session ouverte : la porte d'authentification laisse passer.
  authenticated,

  /// Échec : un message est affiché.
  error,
}

/// État immuable de l'écran d'inscription/connexion.
class AuthState {
  const AuthState({
    this.phase = AuthPhase.idle,
    this.errorMessage,
  });

  final AuthPhase phase;
  final String? errorMessage;

  bool get isSubmitting => phase == AuthPhase.submitting;

  AuthState copyWith({
    AuthPhase? phase,
    String? errorMessage,
    bool clearError = false,
  }) {
    return AuthState(
      phase: phase ?? this.phase,
      errorMessage: clearError ? null : (errorMessage ?? this.errorMessage),
    );
  }
}
