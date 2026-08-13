/// État immuable de l'écran de saisie du jeton opérateur.
class TokenEntryState {
  const TokenEntryState({this.submitting = false, this.errorMessage});

  final bool submitting;
  final String? errorMessage;

  TokenEntryState copyWith({
    bool? submitting,
    String? errorMessage,
    bool clearError = false,
  }) {
    return TokenEntryState(
      submitting: submitting ?? this.submitting,
      errorMessage: clearError ? null : (errorMessage ?? this.errorMessage),
    );
  }
}
