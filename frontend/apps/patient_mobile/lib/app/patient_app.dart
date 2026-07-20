import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../di/providers.dart';
import '../features/onboarding/onboarding_page.dart';
import '../features/orientation/presentation/orientation_page.dart';

/// Racine de l'application patient.
///
/// Pour un nouvel utilisateur (ou après évolution des conditions), l'accueil
/// et l'acceptation des conditions précèdent le parcours d'orientation.
class PatientApp extends ConsumerWidget {
  const PatientApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final consent = ref.watch(consentUpToDateProvider);
    return MaterialApp(
      title: 'Urgence Santé',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.patient(),
      darkTheme: AppTheme.patientDark(),
      // Suit le réglage clair/sombre du système.
      themeMode: ThemeMode.system,
      home: consent.when(
        loading: () => const Scaffold(body: AsyncStateView.loading()),
        error: (_, _) => const OnboardingPage(),
        data: (upToDate) =>
            upToDate ? const OrientationPage() : const OnboardingPage(),
      ),
    );
  }
}
