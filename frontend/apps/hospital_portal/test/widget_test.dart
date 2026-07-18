import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:hospital_portal/app/portal_app.dart';
import 'package:hospital_portal/di/providers.dart';
import 'package:hospital_portal/features/board/domain/model/facility_summary.dart';
import 'package:hospital_portal/features/board/domain/model/service_line.dart';
import 'package:hospital_portal/features/board/domain/repository/portal_repository.dart';

/// Faux repository configurable, substituable au vrai adaptateur API.
class FakePortalRepository implements PortalRepository {
  final List<FacilitySummary> facilities =
      [const FacilitySummary(id: 'f-1', name: 'CHU de Cocody')];
  final List<String> updates = [];

  @override
  Future<List<FacilitySummary>> loadFacilities() async => facilities;

  @override
  Future<List<ServiceLine>> loadBoard(String facilityId) async => const [
        ServiceLine(serviceCode: 'maternity', label: 'Maternité', status: 'UNKNOWN'),
      ];

  @override
  Future<ServiceLine> updateStatus({
    required String facilityId,
    required ServiceLine line,
    required String status,
  }) async {
    updates.add('${line.serviceCode}=$status');
    return line.withUpdate(
      status: status,
      freshness: 'FRESH',
      updatedAt: DateTime.utc(2026, 1, 1, 12),
    );
  }

  @override
  Future<List<HistoryEntry>> history(String facilityId, String serviceCode) async =>
      [HistoryEntry(status: 'AVAILABLE', updatedAt: DateTime.utc(2026, 1, 1, 11))];
}

void main() {
  late FakePortalRepository repository;

  Future<void> pumpApp(WidgetTester tester) async {
    repository = FakePortalRepository();
    await tester.pumpWidget(
      ProviderScope(
        overrides: [portalRepositoryProvider.overrideWithValue(repository)],
        child: const PortalApp(),
      ),
    );
    await tester.pumpAndSettle();
  }

  testWidgets("l'accès démo liste les établissements", (tester) async {
    await pumpApp(tester);

    expect(find.text('Accès agent — démonstration'), findsOneWidget);
    expect(find.text('CHU de Cocody'), findsOneWidget);
  });

  testWidgets('le tableau affiche les services du catalogue', (tester) async {
    await pumpApp(tester);
    await tester.tap(find.text('CHU de Cocody'));
    await tester.pumpAndSettle();

    expect(find.text('Maternité'), findsOneWidget);
    expect(find.text('Non confirmé'), findsOneWidget);
    expect(find.text('Jamais renseigné'), findsOneWidget);
  });

  testWidgets('choisir un statut envoie la mise à jour et horodate',
      (tester) async {
    await pumpApp(tester);
    await tester.tap(find.text('CHU de Cocody'));
    await tester.pumpAndSettle();

    await tester.tap(find.text('Limité'));
    await tester.pumpAndSettle();

    expect(repository.updates, ['maternity=LIMITED']);
    expect(find.textContaining('Mis à jour le'), findsOneWidget);
  });

  testWidgets("l'historique s'affiche dans un panneau", (tester) async {
    await pumpApp(tester);
    await tester.tap(find.text('CHU de Cocody'));
    await tester.pumpAndSettle();

    await tester.tap(find.text('Historique'));
    await tester.pumpAndSettle();

    expect(find.text('Historique — Maternité'), findsOneWidget);
    // « Disponible » apparaît aussi dans la puce de choix derrière le panneau :
    // l'entrée d'historique ajoute une occurrence supplémentaire.
    expect(find.text('Disponible'), findsNWidgets(2));
  });
}
