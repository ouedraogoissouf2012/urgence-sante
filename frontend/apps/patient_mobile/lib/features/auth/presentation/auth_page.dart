import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../di/providers.dart';
import '../../orientation/presentation/widgets/emergency_call_bar.dart';
import 'auth_view_model.dart';
import 'widgets/auth_form.dart';

/// Écran d'entrée : inscription (par défaut) ou connexion, avec une issue
/// d'urgence. Le compte est proposé à l'ouverture, MAIS personne n'est jamais
/// bloqué : « Urgence — continuer sans compte » et les appels SAMU/Pompiers
/// restent toujours accessibles ici.
class AuthPage extends ConsumerStatefulWidget {
  const AuthPage({super.key});

  @override
  ConsumerState<AuthPage> createState() => _AuthPageState();
}

class _AuthPageState extends ConsumerState<AuthPage> {
  bool _registerMode = true;

  void _toggleMode() {
    // Réinitialise l'état du formulaire (efface un éventuel message d'erreur).
    ref.invalidate(authViewModelProvider);
    setState(() => _registerMode = !_registerMode);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Urgence Santé')),
      bottomNavigationBar: const EmergencyCallBar(),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(AppSpacing.lg),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text(
                _registerMode ? 'Créer votre compte' : 'Se connecter',
                style: AppTypography.headline,
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: AppSpacing.xs),
              Text(
                _registerMode
                    ? 'Votre compte vous permettra de préparer votre fiche d\'urgence.'
                    : 'Accédez à votre compte Urgence Santé.',
                style: AppTypography.body,
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: AppSpacing.xl),

              AuthForm(
                key: ValueKey(_registerMode),
                submitLabel: _registerMode ? 'S\'inscrire' : 'Se connecter',
                onSubmit: (vm, phone, password) => _registerMode
                    ? vm.register(phone: phone, password: password)
                    : vm.login(phone: phone, password: password),
              ),

              const SizedBox(height: AppSpacing.md),
              TextButton(
                onPressed: _toggleMode,
                child: Text(_registerMode
                    ? 'J\'ai déjà un compte — me connecter'
                    : 'Créer un compte'),
              ),

              const Divider(height: AppSpacing.xl),

              // Issue d'urgence : ne jamais bloquer quelqu'un en détresse.
              OutlinedButton.icon(
                onPressed: () => ref.read(guestModeProvider.notifier).enter(),
                icon: const Icon(Icons.emergency_outlined),
                label: const Text('Urgence — continuer sans compte'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
