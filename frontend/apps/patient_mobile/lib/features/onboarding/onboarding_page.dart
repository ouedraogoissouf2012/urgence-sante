import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/consent/consent_store.dart';
import '../../di/providers.dart';

/// Accueil : présentation, limites médicales, explication de la localisation
/// et acceptation des conditions (versionnée, persistée).
class OnboardingPage extends ConsumerStatefulWidget {
  const OnboardingPage({super.key});

  @override
  ConsumerState<OnboardingPage> createState() => _OnboardingPageState();
}

class _OnboardingPageState extends ConsumerState<OnboardingPage> {
  bool _accepting = false;

  Future<void> _accept() async {
    if (_accepting) return;
    setState(() => _accepting = true);
    try {
      await ref.read(consentStoreProvider).acceptTerms(currentTermsVersion);
      ref.invalidate(consentUpToDateProvider);
    } finally {
      if (mounted) setState(() => _accepting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(AppSpacing.lg),
          children: [
            const SizedBox(height: AppSpacing.lg),
            const Icon(Icons.local_hospital, size: AppSizing.iconHero),
            const SizedBox(height: AppSpacing.md),
            const Text('Urgence Santé',
                style: AppTypography.headline, textAlign: TextAlign.center),
            const SizedBox(height: AppSpacing.sm),
            const Text(
              'Trouvez rapidement le centre de santé adapté le plus proche '
              'dans le Grand Abidjan, avec itinéraire et appel direct des secours.',
              style: AppTypography.body,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: AppSpacing.lg),
            const InfoCard(
              tone: InfoCardTone.alert,
              icon: Icons.warning_amber_rounded,
              title: 'Limites médicales',
              body: 'Cette application aide à vous orienter mais ne remplace '
                  "pas un avis médical. En cas d'urgence vitale, appelez "
                  'immédiatement le SAMU (185) ou les Pompiers (180).',
            ),
            const SizedBox(height: AppSpacing.md),
            const InfoCard(
              icon: Icons.location_on_outlined,
              title: 'Votre position',
              body: "L'application demandera l'accès à votre position "
                  'uniquement pour trouver les centres les plus proches. '
                  "Elle n'est ni conservée ni partagée. Vous pourrez aussi "
                  'chercher sans localisation précise.',
            ),
            const SizedBox(height: AppSpacing.lg),
            const Text(
              "Conditions d'utilisation — version $currentTermsVersion",
              style: AppTypography.caption,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: AppSpacing.sm),
            FilledButton(
              onPressed: _accepting ? null : _accept,
              child: _accepting
                  ? const SizedBox(
                      height: AppSpacing.lg,
                      width: AppSpacing.lg,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                  : const Text("J'accepte les conditions et je continue"),
            ),
          ],
        ),
      ),
    );
  }
}
