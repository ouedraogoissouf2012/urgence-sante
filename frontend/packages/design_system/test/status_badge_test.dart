import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  Future<void> pump(WidgetTester tester, Widget child) {
    return tester.pumpWidget(MaterialApp(home: Scaffold(body: child)));
  }

  testWidgets("l'information est portée par le texte, pas la couleur seule",
      (tester) async {
    await pump(tester, const StatusBadge(kind: StatusBadgeKind.available));

    expect(find.text('Disponible'), findsOneWidget);
  });

  testWidgets('fromApi mappe les statuts du contrat', (tester) async {
    await pump(tester, StatusBadge.fromApi('SATURATED'));

    expect(find.text('Saturé'), findsOneWidget);
  });

  testWidgets('un statut inconnu est affiché comme non confirmé', (tester) async {
    await pump(tester, StatusBadge.fromApi('WEIRD_VALUE'));

    expect(find.text('Non confirmé'), findsOneWidget);
  });
}
