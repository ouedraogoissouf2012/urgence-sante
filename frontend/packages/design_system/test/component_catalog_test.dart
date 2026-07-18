import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets('chaque entrée du catalogue se monte sans erreur', (tester) async {
    for (final entry in ComponentCatalog.entries) {
      await tester.pumpWidget(
        MaterialApp(
          theme: AppTheme.patient(),
          home: Scaffold(body: Builder(builder: entry.builder)),
        ),
      );

      expect(tester.takeException(), isNull, reason: 'Entrée : ${entry.name}');
    }
  });

  testWidgets("le bouton d'appel déclenche le callback et expose un libellé",
      (tester) async {
    var pressed = false;
    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(
          body: EmergencyCallButton(
            label: 'SAMU',
            phoneNumber: '185',
            onPressed: () => pressed = true,
          ),
        ),
      ),
    );

    await tester.tap(find.byType(EmergencyCallButton));
    expect(pressed, isTrue);
    expect(find.text('SAMU 185'), findsOneWidget);
    expect(
      find.bySemanticsLabel('Appeler le SAMU au 185'),
      findsOneWidget,
    );
  });
}
