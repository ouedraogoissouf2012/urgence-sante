import 'package:app_foundation/app_foundation.dart';
import 'package:flutter/material.dart';

/// Écran d'accueil minimal de l'application patient.
///
/// Le parcours complet (carte, recherche du centre le plus proche, itinéraire,
/// appels d'urgence) est construit à l'issue #14.
class HomePage extends StatelessWidget {
  const HomePage({required this.environment, super.key});

  final AppEnvironment environment;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Urgence Santé')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.local_hospital, size: 72),
            const SizedBox(height: 16),
            const Text("Orientation d'urgence — Grand Abidjan"),
            const SizedBox(height: 8),
            Text('Environnement : ${environment.name}'),
          ],
        ),
      ),
    );
  }
}
