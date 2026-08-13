import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/auth/session_store.dart';
import '../../../di/providers.dart';
import '../domain/auth_failure.dart';
import '../domain/auth_repository.dart';
import 'auth_state.dart';

/// ViewModel d'inscription/connexion (MVVM, flux unidirectionnel).
///
/// Dépendances résolues via les providers (faux en test) ; aucune dépendance
/// widget : testable en pur Dart. En cas de succès, enregistre le jeton dans le
/// stockage sécurisé puis passe la porte d'authentification.
final authViewModelProvider =
    NotifierProvider<AuthViewModel, AuthState>(AuthViewModel.new);

class AuthViewModel extends Notifier<AuthState> {
  AuthRepository get _repository => ref.read(authRepositoryProvider);

  SessionStore get _sessionStore => ref.read(sessionStoreProvider);

  @override
  AuthState build() => const AuthState();

  Future<void> register({required String phone, required String password}) {
    return _submit(() => _repository.register(phone: phone, password: password));
  }

  Future<void> login({required String phone, required String password}) {
    return _submit(() => _repository.login(phone: phone, password: password));
  }

  /// Efface le message d'erreur affiché dès que l'utilisateur reprend la
  /// saisie : un message renvoyé par une soumission précédente ne doit pas
  /// rester à l'écran sur des champs déjà modifiés. N'agit que sur un état
  /// d'erreur — jamais sur une soumission en cours ni une session ouverte —
  /// donc appelable sans risque à chaque frappe.
  void dismissError() {
    if (state.phase == AuthPhase.error) {
      state = state.copyWith(phase: AuthPhase.idle, clearError: true);
    }
  }

  Future<void> _submit(Future<String> Function() action) async {
    state = state.copyWith(phase: AuthPhase.submitting, clearError: true);
    try {
      final String token = await action();
      await _sessionStore.saveToken(token);
      // Invalide l'état de session observé par la porte d'authentification.
      ref.invalidate(sessionTokenProvider);
      state = state.copyWith(phase: AuthPhase.authenticated);
    } on AuthException catch (error) {
      state = state.copyWith(phase: AuthPhase.error, errorMessage: error.message);
    } catch (_) {
      state = state.copyWith(
          phase: AuthPhase.error,
          errorMessage: 'Une erreur est survenue. Réessayez.');
    }
  }
}
