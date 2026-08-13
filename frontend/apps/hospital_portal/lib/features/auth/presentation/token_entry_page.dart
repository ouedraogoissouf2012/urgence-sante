import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'token_entry_state.dart';
import 'token_entry_view_model.dart';

/// Écran de connexion du portail : saisie du jeton opérateur fourni par
/// l'établissement (aucun identifiant/mot de passe — voir issue #164 pour le
/// provisionnement de ce jeton).
class TokenEntryPage extends ConsumerStatefulWidget {
  const TokenEntryPage({super.key});

  @override
  ConsumerState<TokenEntryPage> createState() => _TokenEntryPageState();
}

class _TokenEntryPageState extends ConsumerState<TokenEntryPage> {
  final TextEditingController _controller = TextEditingController();

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _submit() {
    ref.read(tokenEntryViewModelProvider.notifier).submit(_controller.text);
  }

  @override
  Widget build(BuildContext context) {
    final TokenEntryState state = ref.watch(tokenEntryViewModelProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Portail hospitalier')),
      body: SafeArea(
        child: Center(
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 420),
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(AppSpacing.lg),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  const Text(
                    'Connexion agent',
                    style: AppTypography.headline,
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: AppSpacing.xs),
                  const Text(
                    "Entrez le jeton d'accès fourni par votre établissement. "
                    'Si vous étiez déjà connecté, votre session a peut-être expiré.',
                    style: AppTypography.body,
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: AppSpacing.xl),
                  AppTextField(
                    label: "Jeton d'accès",
                    controller: _controller,
                    obscure: true,
                    enabled: !state.submitting,
                    errorText: state.errorMessage,
                    textInputAction: TextInputAction.done,
                    onSubmitted: (_) => _submit(),
                  ),
                  const SizedBox(height: AppSpacing.md),
                  FilledButton(
                    onPressed: state.submitting ? null : _submit,
                    child: state.submitting
                        ? const SizedBox(
                            width: 20,
                            height: 20,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : const Text('Continuer'),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
