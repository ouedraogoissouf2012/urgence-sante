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

/// Écriture qui échoue toujours, pour vérifier qu'un échec n'est jamais
/// silencieux (issue #140).
class _ThrowingKeyValueStore implements KeyValueStore {
  @override
  Future<String?> read(String key) async => null;

  @override
  Future<void> write(String key, String value) async =>
      throw Exception('écriture impossible (simulée)');
}

Finder _saveButtonFinder() => find.ancestor(
      // FilledButton.icon() renvoie une sous-classe privée : find.byType
      // exige une correspondance exacte de type, d'où byWidgetPredicate (is).
      of: find.text('Enregistrer'),
      matching: find.byWidgetPredicate((widget) => widget is FilledButton),
    );

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

    final saveButton = _saveButtonFinder();
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

  testWidgets(
      'un échec d\'écriture affiche un SnackBar et ne ferme PAS la page',
      (tester) async {
    tester.view.physicalSize = const Size(1080, 3600);
    tester.view.devicePixelRatio = 1.0;
    addTearDown(tester.view.resetPhysicalSize);
    addTearDown(tester.view.resetDevicePixelRatio);

    await tester.pumpWidget(_appOpeningForm(_ThrowingKeyValueStore()));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Ouvrir la fiche'));
    await tester.pumpAndSettle();

    final saveButton = _saveButtonFinder();
    await tester.tap(saveButton);
    await tester.pumpAndSettle();

    // Toujours sur le formulaire : la page ne s'est PAS fermée sur un échec.
    expect(find.text('Ma fiche d\'urgence'), findsOneWidget);
    // L'utilisateur est prévenu : jamais d'échec silencieux.
    expect(find.text("Échec de l'enregistrement. Réessayez."), findsOneWidget);
    // Le bouton redevient utilisable (pas bloqué en "sauvegarde" indéfiniment).
    expect(tester.widget<FilledButton>(saveButton).onPressed, isNotNull);
  });
}
