import 'package:flutter/widgets.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:hospital_portal/app/portal_app.dart';
import 'package:hospital_portal/di/providers.dart';
import 'package:hospital_portal/features/board/domain/model/facility_summary.dart';
import 'package:hospital_portal/features/board/domain/model/service_line.dart';
import 'package:hospital_portal/features/board/domain/repository/portal_repository.dart';
import 'package:integration_test/integration_test.dart';

/// Test E2E NAVIGATEUR du portail hospitalier (moteur web réel) :
/// accès agent → sélection établissement → tableau des services →
/// mise à jour d'un statut (horodatée) → consultation de l'historique.
///
/// Exécution (depuis apps/hospital_portal, chromedriver actif sur :4444) :
///   flutter drive --driver=test_driver/integration_test.dart \
///     --target=integration_test/portail_e2e_test.dart \
///     -d web-server --browser-name=chrome --headless
class _Repo implements PortalRepository {
  final List<String> updates = [];

  @override
  Future<List<FacilitySummary>> loadFacilities() async =>
      const [FacilitySummary(id: 'f-1', name: 'CHU de Cocody')];

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
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('parcours agent complet dans le navigateur', (tester) async {
    final repo = _Repo();
    await tester.binding.setSurfaceSize(const Size(600, 1000));

    await tester.pumpWidget(ProviderScope(
      overrides: [portalRepositoryProvider.overrideWithValue(repo)],
      child: const PortalApp(),
    ));
    await tester.pumpAndSettle();

    // 1) Accès agent : liste des établissements.
    expect(find.text('Accès agent — démonstration'), findsOneWidget);
    await tester.tap(find.text('CHU de Cocody'));
    await tester.pumpAndSettle();

    // 2) Tableau des services : la maternité est « Non confirmé ».
    expect(find.text('Disponibilité des services'), findsOneWidget);
    expect(find.text('Maternité'), findsOneWidget);
    expect(find.text('Jamais renseigné'), findsOneWidget);

    // 3) Mise à jour du statut → envoi + horodatage.
    await tester.tap(find.text('Limité'));
    await tester.pumpAndSettle();
    expect(repo.updates, ['maternity=LIMITED']);
    expect(find.textContaining('Mis à jour le'), findsOneWidget);

    // 4) Historique dans un panneau.
    await tester.tap(find.text('Historique'));
    await tester.pumpAndSettle();
    expect(find.text('Historique — Maternité'), findsOneWidget);
  });
}
