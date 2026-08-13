import 'package:app_foundation/app_foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../di/providers.dart';
import 'token_entry_state.dart';

/// ViewModel de l'écran de saisie du jeton opérateur (MVVM, flux
/// unidirectionnel). Aucun appel serveur : le portail ne dispose d'aucun
/// point de connexion (le jeton est provisionné hors-bande, voir issue #164)
/// — la seule validation possible ici est locale (non vide) ; la validité
/// réelle n'est confirmée que par la première écriture protégée.
final tokenEntryViewModelProvider =
    NotifierProvider<TokenEntryViewModel, TokenEntryState>(TokenEntryViewModel.new);

class TokenEntryViewModel extends Notifier<TokenEntryState> {
  TokenStore get _tokenStore => ref.read(tokenStoreProvider);

  @override
  TokenEntryState build() => const TokenEntryState();

  Future<void> submit(String rawToken) async {
    if (state.submitting) {
      return;
    }
    final String token = rawToken.trim();
    if (token.isEmpty) {
      state = state.copyWith(errorMessage: 'Le jeton est requis.');
      return;
    }
    state = state.copyWith(submitting: true, clearError: true);
    try {
      await _tokenStore.saveToken(token);
      // Fait basculer la porte d'authentification (PortalApp) vers le
      // tableau. `submitting` est quand même remis à `false` ci-dessous :
      // tokenEntryViewModelProvider n'est PAS `.autoDispose`, son état
      // survit à la déconnexion suivante — sans ce reset, la prochaine
      // ouverture de cet écran resterait bloquée avec le bouton désactivé
      // et le spinner figé (constaté en revue, pas une précaution théorique).
      ref.invalidate(sessionTokenProvider);
      state = state.copyWith(submitting: false);
    } catch (_) {
      state = state.copyWith(
        submitting: false,
        errorMessage: "Impossible d'enregistrer le jeton. Réessayez.",
      );
    }
  }
}
