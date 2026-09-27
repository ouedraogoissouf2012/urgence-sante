import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../auth_state.dart';
import '../auth_view_model.dart';

/// Formulaire d'authentification partagé (inscription et connexion) : téléphone,
/// mot de passe, bouton d'action, message d'erreur. Une validation locale
/// (champs requis, mot de passe ≥ 8 caractères — même borne que le serveur)
/// évite un aller-retour réseau inutile pour un formulaire manifestement
/// invalide ; le reste de la logique est délégué au [AuthViewModel].
class AuthForm extends ConsumerStatefulWidget {
  const AuthForm({
    required this.submitLabel,
    required this.onSubmit,
    super.key,
  });

  final String submitLabel;

  /// Action déclenchée avec les valeurs saisies (register ou login du VM).
  final Future<void> Function(AuthViewModel vm, String phone, String password) onSubmit;

  @override
  ConsumerState<AuthForm> createState() => _AuthFormState();
}

class _AuthFormState extends ConsumerState<AuthForm> {
  /// Même borne que le serveur (`PatientService.MIN_PASSWORD_LENGTH`) : un mot
  /// de passe trop court ne peut de toute façon jamais correspondre à un compte
  /// existant, autant l'annoncer localement plutôt que via un aller-retour réseau.
  static const int _minPasswordLength = 8;

  final _phone = TextEditingController();
  final _password = TextEditingController();

  String? _phoneError;
  String? _passwordError;

  @override
  void initState() {
    super.initState();
    _phone.addListener(_clearPhoneError);
    _password.addListener(_clearPasswordError);
  }

  @override
  void dispose() {
    _phone.dispose();
    _password.dispose();
    super.dispose();
  }

  void _clearPhoneError() {
    if (_phoneError != null) setState(() => _phoneError = null);
    _dismissSubmitError();
  }

  void _clearPasswordError() {
    if (_passwordError != null) setState(() => _passwordError = null);
    _dismissSubmitError();
  }

  /// Efface aussi le message d'erreur global (l'{@link InfoCard} renvoyée par
  /// une soumission précédente) dès que l'utilisateur reprend la saisie —
  /// sinon il resterait affiché sur des champs déjà corrigés jusqu'à la
  /// prochaine tentative. Sans effet hors état d'erreur (garde côté VM).
  void _dismissSubmitError() {
    ref.read(authViewModelProvider.notifier).dismissError();
  }

  void _submit(AuthViewModel vm) {
    final String phone = _phone.text.trim();
    final String password = _password.text;

    final String? phoneError =
        phone.isEmpty ? 'Le numéro de téléphone est requis.' : null;
    final String? passwordError = password.length < _minPasswordLength
        ? 'Le mot de passe doit contenir au moins $_minPasswordLength caractères.'
        : null;

    if (phoneError != null || passwordError != null) {
      setState(() {
        _phoneError = phoneError;
        _passwordError = passwordError;
      });
      return;
    }

    widget.onSubmit(vm, phone, password);
  }

  @override
  Widget build(BuildContext context) {
    final AuthState state = ref.watch(authViewModelProvider);
    final AuthViewModel vm = ref.read(authViewModelProvider.notifier);
    final bool busy = state.isSubmitting;

    return AutofillGroup(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          AppTextField(
            label: 'Téléphone',
            hint: '+225 XX XX XX XX XX',
            controller: _phone,
            enabled: !busy,
            errorText: _phoneError,
            keyboardType: TextInputType.phone,
            textInputAction: TextInputAction.next,
            autofillHints: const [AutofillHints.telephoneNumber],
            inputFormatters: [
              FilteringTextInputFormatter.allow(RegExp(r'[0-9+\s().\-]')),
            ],
          ),
          const SizedBox(height: AppSpacing.md),
          AppTextField(
            label: 'Mot de passe',
            hint: 'Au moins 8 caractères',
            controller: _password,
            enabled: !busy,
            errorText: _passwordError,
            obscure: true,
            textInputAction: TextInputAction.done,
            autofillHints: const [AutofillHints.password],
            onSubmitted: (_) => busy ? null : _submit(vm),
          ),
          if (state.errorMessage != null) ...[
            const SizedBox(height: AppSpacing.md),
            InfoCard(
              icon: Icons.error_outline,
              tone: InfoCardTone.alert,
              title: 'Impossible de continuer',
              body: state.errorMessage!,
            ),
          ],
          const SizedBox(height: AppSpacing.lg),
          FilledButton(
            onPressed: busy ? null : () => _submit(vm),
            child: busy
                ? const SizedBox(
                    height: AppSizing.iconMarker,
                    width: AppSizing.iconMarker,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : Text(widget.submitLabel),
          ),
        ],
      ),
    );
  }
}
