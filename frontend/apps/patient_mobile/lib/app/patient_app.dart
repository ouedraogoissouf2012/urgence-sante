import 'package:app_foundation/app_foundation.dart';
import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';

import '../features/home/home_page.dart';

/// Racine de l'application patient.
class PatientApp extends StatelessWidget {
  const PatientApp({required this.config, super.key});

  final AppConfig config;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Urgence Santé',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.patient(),
      home: HomePage(environment: config.environment),
    );
  }
}
