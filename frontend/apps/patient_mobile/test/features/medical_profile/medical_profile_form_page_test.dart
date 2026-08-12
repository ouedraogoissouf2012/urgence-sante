import 'dart:async';

import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/core/storage/key_value_store.dart';
import 'package:patient_mobile/di/providers.dart';
import 'package:patient_mobile/features/medical_profile/data/local_medical_profile_store.dart';
import 'package:patient_mobile/features/medical_profile/presentation/medical_profile_form_page.dart';

import '../auth/support/auth_fakes.dart' show InMemoryKeyValueStore;

/// Écriture bloquée jusqu'à [release] : simule une sauvegarde en cours pour
/// pouvoir taper deux fois "Enregistrer" pendant qu'elle est en vol.
class _SlowKeyValueStore implements KeyValueStore {
  final Map<String, String> data = {};
  int writeCount = 0;
  final Completer<void> _gate = Completer<void>();

  @override
  Future<String?> read(String key) async => data[key];

  @override
  Future<void> write(String key, String value) async {
    writeCount++;
    await _gate.future;
    data[key] = value;
  }

  void release() => _gate.complete();
}

Widget _appOpeningForm(KeyValueStore secureKv) {
  return ProviderScope(
    overrides: [
      profileStoreProvider.overrideWithValue(
          LocalMedicalProfileStore(secureKv, InMemoryKeyValueStore())),
    ],
    child: MaterialApp(
      theme: AppTheme.patient(),
      home: Builder(
        builder: (context) => Scaffold(
          body: Center(
            child: FilledButton(
              onPressed: () => Navigator.of(context).push<void>(
                MaterialPageRoute(builder: (_) => const MedicalProfileFormPage()),
              ),
              child: const Text('Ouvrir la fiche'),
            ),
          ),
        ),
      ),
    ),
  );
}

void main() {
  testWidgets(
      'un double-tap sur Enregistrer ne déclenche qu\'une seule écriture',
      (tester) async {
    // Formulaire long (7 champs) : agrandit la surface de test pour que le
    // bouton "Enregistrer" soit monté sans avoir à scroller.
    tester.view.physicalSize = const Size(1080, 3600);
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);

    final slowKv = _SlowKeyValueStore();
    await tester.pumpWidget(_appOpeningForm(slowKv));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Ouvrir la fiche'));
    await tester.pumpAndSettle();

    // FilledButton.icon() renvoie une sous-classe privée : find.byType exige
    // une correspondance exacte de type, d'où le byWidgetPredicate (is).
    final saveButton = find.ancestor(
      of: find.text('Enregistrer'),
      matching: find.byWidgetPredicate((widget) => widget is FilledButton),
    );
    await tester.tap(saveButton);
    await tester.pump(); // démarre la sauvegarde ; l'écriture reste en vol (gate fermée).

    // Le bouton est désactivé pendant la sauvegarde : le second tap est un no-op.
    expect(tester.widget<FilledButton>(saveButton).onPressed, isNull);
    await tester.tap(saveButton);
    await tester.pump();

    expect(slowKv.writeCount, 1);

    slowKv.release();
    await tester.pumpAndSettle();

    // La sauvegarde a réussi : retour à la page précédente.
    expect(find.text('Ouvrir la fiche'), findsOneWidget);
  });
}
