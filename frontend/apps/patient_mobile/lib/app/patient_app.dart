import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';

import '../features/orientation/presentation/orientation_page.dart';

/// Racine de l'application patient.
///
/// La configuration d'environnement est fournie par les providers (bootstrap).
class PatientApp extends StatelessWidget {
  const PatientApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Urgence Santé',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.patient(),
      home: const OrientationPage(),
    );
  }
}
