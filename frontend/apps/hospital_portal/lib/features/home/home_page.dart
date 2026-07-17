import 'package:app_foundation/app_foundation.dart';
import 'package:flutter/material.dart';

/// Écran d'accueil minimal du portail hospitalier.
///
/// La connexion agent et la saisie de disponibilité sont construites à
/// l'issue #15.
class HomePage extends StatelessWidget {
  const HomePage({required this.environment, super.key});

  final AppEnvironment environment;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Portail hospitalier')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.medical_information, size: 72),
            const SizedBox(height: 16),
            const Text('Saisie de disponibilité des services'),
            const SizedBox(height: 8),
            Text('Environnement : ${environment.name}'),
          ],
        ),
      ),
    );
  }
}
