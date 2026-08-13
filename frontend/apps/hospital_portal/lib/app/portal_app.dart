import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../di/providers.dart';
import '../features/auth/presentation/token_entry_page.dart';
import '../features/board/presentation/portal_page.dart';

/// Racine du portail hospitalier.
///
/// Porte d'authentification : jeton opérateur absent → écran de connexion
/// (issue #163) ; jeton présent → tableau de disponibilité. La configuration
/// d'environnement est fournie par les providers (bootstrap).
class PortalApp extends ConsumerWidget {
  const PortalApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return MaterialApp(
      title: 'Portail hospitalier — Urgence Santé',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.hospital(),
      home: _home(ref),
    );
  }

  Widget _home(WidgetRef ref) {
    final session = ref.watch(sessionTokenProvider);
    return session.when(
      loading: () => const Scaffold(body: AsyncStateView.loading()),
      // Une erreur de lecture du stockage sécurisé est traitée comme une
      // absence de jeton : on ne bloque jamais l'agent derrière un état
      // illisible, on lui propose simplement de se (re)connecter.
      error: (_, _) => const TokenEntryPage(),
      data: (token) =>
          (token != null && token.isNotEmpty) ? const PortalPage() : const TokenEntryPage(),
    );
  }
}
