import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:hospital_portal/features/board/domain/model/service_line.dart';
import 'package:hospital_portal/features/board/presentation/widgets/service_line_tile.dart';

/// Accessibilité et robustesse de la tuile de service (issue #47) : activation
/// au clavier, sémantique des actions et absence d'overflow en grande police.
void main() {
  const line = ServiceLine(
    serviceCode: 'maternity',
    label: 'Maternité',
    status: 'AVAILABLE',
  );

  Widget host({double width = 500, double textScale = 1.0}) {
    return MediaQuery(
      data: MediaQueryData(textScaler: TextScaler.linear(textScale)),
      child: MaterialApp(
        home: Scaffold(
          body: Center(
            child: SizedBox(
              width: width,
              child: SingleChildScrollView(
                child: ServiceLineTile(
                  line: line,
                  updating: false,
                  onStatusSelected: (value) => _selected = value,
                  onHistoryRequested: () {},
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  testWidgets('un choix de statut est activable au clavier', (tester) async {
    _selected = null;
    await tester.pumpWidget(host());

    // Focus sur le premier élément focusable, puis activation par Espace.
    await tester.sendKeyEvent(LogicalKeyboardKey.tab);
    await tester.pump();
    await tester.sendKeyEvent(LogicalKeyboardKey.space);
    await tester.pump();

    expect(_selected, isNotNull,
        reason: 'un ChoiceChip doit être atteignable et activable au clavier');
  });

  testWidgets('les actions portent une sémantique explicite', (tester) async {
    await tester.pumpWidget(host());

    expect(find.bySemanticsLabel('Marquer Maternité : Limité'), findsOneWidget);
    expect(find.bySemanticsLabel('Historique de Maternité'), findsOneWidget);
  });

  testWidgets('aucun overflow en très grande police sur écran étroit',
      (tester) async {
    await tester.pumpWidget(host(width: 340, textScale: 3.0));
    await tester.pump();

    expect(tester.takeException(), isNull);
  });
}

String? _selected;
