import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../di/providers.dart';
import '../features/auth/presentation/auth_page.dart';
import '../features/onboarding/onboarding_page.dart';
import '../features/orientation/presentation/orientation_page.dart';

/// Racine de l'application patient.
///
/// Deux portes en séquence, sans jamais bloquer une urgence :
/// 1. AUTHENTIFICATION — un compte est proposé à l'ouverture ; sans session ni
///    mode invité (« Urgence — continuer sans compte »), on affiche l'écran
///    d'inscription/connexion (qui porte lui-même l'issue d'urgence et les
///    appels SAMU/Pompiers).
/// 2. CONSENTEMENT — accueil et acceptation des conditions avant le parcours.
class PatientApp extends ConsumerWidget {
  const PatientApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return MaterialApp(
      title: 'Urgence Santé',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.patient(),
      darkTheme: AppTheme.patientDark(),
      // Suit le réglage clair/sombre du système.
      themeMode: ThemeMode.system,
      home: _home(ref),
    );
  }

  Widget _home(WidgetRef ref) {
    final bool guest = ref.watch(guestModeProvider);
    final session = ref.watch(sessionTokenProvider);

    return session.when(
      loading: () => const Scaffold(body: AsyncStateView.loading()),
      // En cas d'erreur de lecture du stockage sécurisé, on ne bloque pas
      // l'accès : on propose l'authentification (l'utilisateur peut passer).
      error: (_, _) => guest ? const _ConsentGate() : const AuthPage(),
      data: (token) => (token != null && token.isNotEmpty) || guest
          ? const _ConsentGate()
          : const AuthPage(),
    );
  }
}

/// Deuxième porte : consentement puis parcours d'orientation.
class _ConsentGate extends ConsumerWidget {
  const _ConsentGate();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final consent = ref.watch(consentUpToDateProvider);
    return consent.when(
      loading: () => const Scaffold(body: AsyncStateView.loading()),
      error: (_, _) => const OnboardingPage(),
      data: (upToDate) =>
          upToDate ? const OrientationPage() : const OnboardingPage(),
    );
  }
}
