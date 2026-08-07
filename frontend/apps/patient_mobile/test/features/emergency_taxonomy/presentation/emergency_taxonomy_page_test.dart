import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/core/calls/emergency_caller.dart';
import 'package:patient_mobile/di/providers.dart';
import 'package:patient_mobile/features/emergency_taxonomy/domain/model/emergency_category.dart';
import 'package:patient_mobile/features/emergency_taxonomy/domain/repository/emergency_taxonomy_repository.dart';
import 'package:patient_mobile/features/emergency_taxonomy/presentation/emergency_taxonomy_page.dart';

class _FakeTaxonomy implements EmergencyTaxonomyRepository {
  @override
  Future<List<EmergencyCategory>> fetchTaxonomy() async => const [
        EmergencyCategory(
          id: 'respiratoires',
          label: 'Urgences respiratoires',
          directCallOnly: false,
          symptoms: [Symptom(id: 'crise-asthme', label: "Crise d'asthme")],
          serviceCodes: ['pulmonology'],
        ),
        EmergencyCategory(
          id: 'accidents',
          label: 'Accidents et traumatologie',
          directCallOnly: true,
          directCallMessage: 'Appelez immédiatement les secours.',
          symptoms: [Symptom(id: 'fracture', label: 'Fracture')],
          serviceCodes: ['ortho_trauma'],
        ),
      ];
}

class _RecordingCaller implements EmergencyCaller {
  final List<String> calls = [];

  @override
  Future<void> call(String phoneNumber) async => calls.add(phoneNumber);
}

Widget _page(_RecordingCaller caller) => ProviderScope(
      overrides: [
        emergencyTaxonomyRepositoryProvider.overrideWithValue(_FakeTaxonomy()),
        emergencyCallerProvider.overrideWithValue(caller),
      ],
      child: const MaterialApp(home: EmergencyTaxonomyPage()),
    );

void main() {
  testWidgets('niveau 1 : les grandes catégories et les appels d\'urgence',
      (tester) async {
    await tester.pumpWidget(_page(_RecordingCaller()));
    await tester.pumpAndSettle();

    expect(find.text('Quelle est votre urgence ?'), findsOneWidget);
    expect(find.text('Urgences respiratoires'), findsOneWidget);
    expect(find.text('Accidents et traumatologie'), findsOneWidget);
    // La barre d'appel d'urgence reste toujours présente.
    expect(find.text('SAMU 185'), findsOneWidget);
  });

  testWidgets('ouvrir une catégorie affiche ses symptômes (niveau 2)',
      (tester) async {
    await tester.pumpWidget(_page(_RecordingCaller()));
    await tester.pumpAndSettle();

    await tester.tap(find.text('Urgences respiratoires'));
    await tester.pumpAndSettle();

    expect(find.text("Crise d'asthme"), findsOneWidget);
  });

  testWidgets('une catégorie « appel direct » propose les secours, sans recherche',
      (tester) async {
    final caller = _RecordingCaller();
    await tester.pumpWidget(_page(caller));
    await tester.pumpAndSettle();

    await tester.tap(find.text('Accidents et traumatologie'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Fracture'));
    await tester.pumpAndSettle();

    // Feuille d'appel direct : message + boutons SAMU/Pompiers.
    expect(find.textContaining('Appelez immédiatement les secours'), findsOneWidget);
    final samu = find.text('SAMU 185');
    expect(samu, findsWidgets); // barre + feuille
    await tester.tap(samu.last);
    expect(caller.calls, contains('185'));
  });
}
