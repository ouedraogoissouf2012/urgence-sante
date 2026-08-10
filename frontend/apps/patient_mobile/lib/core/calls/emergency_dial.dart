import 'package:flutter/material.dart';

import 'emergency_caller.dart';

/// Déclenche un appel d'urgence et, si le composeur ne peut être ouvert, en
/// informe l'utilisateur — un appel d'urgence ne doit JAMAIS échouer en silence
/// (issue #129). Point unique partagé par la barre d'appel et l'appel direct.
Future<void> dialEmergency(
    BuildContext context, EmergencyCaller caller, String number) async {
  try {
    await caller.call(number);
  } catch (_) {
    if (context.mounted) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(
        content: Text('Impossible de lancer l\'appel. Composez le $number.'),
      ));
    }
  }
}
