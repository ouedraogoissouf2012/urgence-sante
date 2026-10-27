import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../domain/international_phone.dart';
import '../../domain/phone_country.dart';
import '../auth_state.dart';
import '../auth_view_model.dart';
import 'phone_number_field.dart';

/// Formulaire d'authentification partagé (inscription et connexion) : téléphone
/// en deux champs (indicatif + numéro national), mot de passe, bouton
/// d'action, message d'erreur. Une validation locale (numéro conforme au plan
/// du pays, mot de passe ≥ 8 caractères — même borne que le serveur) évite un
/// aller-retour réseau inutile ; le numéro envoyé est TOUJOURS le format
/// international composé par le domaine, à l'inscription comme à la connexion
/// — condition pour que la connexion retrouve le compte créé.
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
  PhoneCountry _country = PhoneCountries.defaultCountry;

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

  /// Changer d'indicatif remet en forme les chiffres déjà saisis selon le
  /// nouveau pays et efface l'erreur devenue obsolète.
  void _changeCountry(PhoneCountry country) {
    setState(() => _country = country);
    _phone.text = formatNationalNumber(country, typedNationalDigits(country, _phone.text));
    _clearPhoneError();
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
    final PhoneValidation phone = validateInternationalPhone(_country, _phone.text);
    final String password = _password.text;

    final String? phoneError = phone.error;
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

    widget.onSubmit(vm, phone.e164!, password);
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
          PhoneNumberField(
            country: _country,
            controller: _phone,
            onCountryChanged: _changeCountry,
            enabled: !busy,
            errorText: _phoneError,
            textInputAction: TextInputAction.next,
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
