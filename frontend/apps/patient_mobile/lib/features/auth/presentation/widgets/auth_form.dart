import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../auth_state.dart';
import '../auth_view_model.dart';

/// Formulaire d'authentification partagé (inscription et connexion) : téléphone,
/// mot de passe, bouton d'action, message d'erreur. La logique est déléguée au
/// [AuthViewModel] ; ce widget ne fait que collecter la saisie.
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
  final _phone = TextEditingController();
  final _password = TextEditingController();

  @override
  void dispose() {
    _phone.dispose();
    _password.dispose();
    super.dispose();
  }

  void _submit(AuthViewModel vm) {
    widget.onSubmit(vm, _phone.text.trim(), _password.text);
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
