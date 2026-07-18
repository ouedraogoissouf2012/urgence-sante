import 'package:app_foundation/app_foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'app/patient_app.dart';
import 'di/providers.dart';

/// Démarrage commun à tous les environnements de l'application patient.
///
/// Chaque point d'entrée (`main_development.dart`, `main_staging.dart`,
/// `main_production.dart`) appelle cette fonction avec la configuration adaptée.
Future<void> bootstrap(AppConfig config) async {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(
    ProviderScope(
      overrides: [appConfigProvider.overrideWithValue(config)],
      child: const PatientApp(),
    ),
  );
}
