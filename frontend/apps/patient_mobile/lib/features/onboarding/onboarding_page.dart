import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/consent/consent_store.dart';
import '../../di/providers.dart';

/// Accueil : présentation, limites médicales, explication de la localisation
/// et acceptation des conditions (versionnée, persistée).
class OnboardingPage extends ConsumerWidget {
  const OnboardingPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(AppSpacing.lg),
          children: [
            const SizedBox(height: AppSpacing.lg),
            const Icon(Icons.local_hospital, size: 64),
            const SizedBox(height: AppSpacing.md),
            const Text('Urgence Santé',
                style: AppTypography.headline, textAlign: TextAlign.center),
            const SizedBox(height: AppSpacing.sm),
            const Text(
              "Trouvez rapidement le centre de santé adapté le plus proche "
              'dans le Grand Abidjan, avec itinéraire et appel direct des secours.',
              style: AppTypography.body,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: AppSpacing.lg),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(AppSpacing.md),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Limites médicales',
                        style: AppTypography.title
                            .copyWith(color: AppColors.emergencyCall)),
                    const SizedBox(height: AppSpacing.xs),
                    const Text(
                      'Cette application aide à vous orienter mais ne remplace '
                      "pas un avis médical. En cas d'urgence vitale, appelez "
                      'immédiatement le SAMU (185) ou les Pompiers (180).',
                      style: AppTypography.body,
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: AppSpacing.md),
            const Card(
              child: Padding(
                padding: EdgeInsets.all(AppSpacing.md),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Votre position', style: AppTypography.title),
                    SizedBox(height: AppSpacing.xs),
                    Text(
                      "L'application demandera l'accès à votre position "
                      'uniquement pour trouver les centres les plus proches. '
                      "Elle n'est ni conservée ni partagée. Vous pourrez aussi "
                      'chercher sans localisation précise.',
                      style: AppTypography.body,
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: AppSpacing.lg),
            const Text(
              "Conditions d'utilisation — version $currentTermsVersion",
              style: AppTypography.caption,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: AppSpacing.sm),
            FilledButton(
              onPressed: () async {
                await ref.read(consentStoreProvider).acceptTerms(currentTermsVersion);
                ref.invalidate(consentUpToDateProvider);
              },
              child: const Text("J'accepte les conditions et je continue"),
            ),
          ],
        ),
      ),
    );
  }
}
