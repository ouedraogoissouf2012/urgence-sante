import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';

import '../features/board/presentation/portal_page.dart';

/// Racine du portail hospitalier.
///
/// La configuration d'environnement est fournie par les providers (bootstrap).
class PortalApp extends StatelessWidget {
  const PortalApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Portail hospitalier — Urgence Santé',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.hospital(),
      home: const PortalPage(),
    );
  }
}
