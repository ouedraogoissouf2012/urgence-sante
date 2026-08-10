import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/di/providers.dart';
import 'package:patient_mobile/features/medical_profile/data/local_medical_profile_store.dart';
import 'package:patient_mobile/features/medical_profile/presentation/emergency_card_page.dart';

import '../auth/support/auth_fakes.dart' show InMemoryKeyValueStore;

Widget _app(InMemoryKeyValueStore secureKv, {InMemoryKeyValueStore? legacyKv}) {
  return ProviderScope(
    overrides: [
      profileStoreProvider.overrideWithValue(
          LocalMedicalProfileStore(secureKv, legacyKv ?? InMemoryKeyValueStore())),
    ],
    child: MaterialApp(theme: AppTheme.patient(), home: const EmergencyCardPage()),
  );
}

void main() {
  testWidgets('fiche vide : invite à créer', (tester) async {
    await tester.pumpWidget(_app(InMemoryKeyValueStore()));
    await tester.pumpAndSettle();

    expect(find.text('Créer ma fiche'), findsOneWidget);
  });

  testWidgets('une fiche remplie (stockage chiffré) s\'affiche en grand', (tester) async {
    final kv = InMemoryKeyValueStore()
      ..data['medical_profile_v1'] =
          '{"version":1,"fullName":"Awa Koné","bloodType":"O-","allergies":"Pénicilline"}';

    await tester.pumpWidget(_app(kv));
    await tester.pumpAndSettle();

    expect(find.text('Awa Koné'), findsOneWidget);
    expect(find.textContaining('O−'), findsOneWidget); // libellé du groupe
    expect(find.text('Pénicilline'), findsOneWidget);
    expect(find.text('Créer ma fiche'), findsNothing);
  });

  testWidgets(
      'une fiche d\'une version antérieure au correctif #128, encore en clair, '
      's\'affiche aussi (migration transparente)', (tester) async {
    final legacyKv = InMemoryKeyValueStore()
      ..data['medical_profile_v1'] =
          '{"version":1,"fullName":"Awa Koné","bloodType":"O-","allergies":"Pénicilline"}';

    await tester.pumpWidget(_app(InMemoryKeyValueStore(), legacyKv: legacyKv));
    await tester.pumpAndSettle();

    expect(find.text('Awa Koné'), findsOneWidget);
    expect(find.textContaining('O−'), findsOneWidget);
    expect(find.text('Pénicilline'), findsOneWidget);
  });

  testWidgets('effacer demande confirmation puis vide la fiche', (tester) async {
    final kv = InMemoryKeyValueStore()
      ..data['medical_profile_v1'] = '{"version":1,"allergies":"Iode"}';

    await tester.pumpWidget(_app(kv));
    await tester.pumpAndSettle();

    await tester.tap(find.byTooltip('Effacer'));
    await tester.pumpAndSettle();
    expect(find.text('Effacer la fiche ?'), findsOneWidget);

    await tester.tap(find.widgetWithText(FilledButton, 'Effacer'));
    await tester.pumpAndSettle();

    expect(find.text('Créer ma fiche'), findsOneWidget);
  });
}
