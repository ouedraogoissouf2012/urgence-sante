import 'package:app_foundation/app_foundation.dart';
import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';

import '../features/home/home_page.dart';

/// Racine du portail hospitalier.
class PortalApp extends StatelessWidget {
  const PortalApp({required this.config, super.key});

  final AppConfig config;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Portail hospitalier — Urgence Santé',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.hospital(),
      home: HomePage(environment: config.environment),
    );
  }
}
