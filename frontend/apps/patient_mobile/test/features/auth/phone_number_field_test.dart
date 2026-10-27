import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/features/auth/domain/phone_country.dart';
import 'package:patient_mobile/features/auth/presentation/widgets/phone_number_field.dart';

/// Monte le champ avec un indicatif piloté par le test (comme le fait AuthForm).
Future<TextEditingController> _pump(
  WidgetTester tester, {
  ValueChanged<PhoneCountry>? onChanged,
}) async {
  final controller = TextEditingController();
  PhoneCountry country = PhoneCountries.defaultCountry;
  await tester.pumpWidget(
    MaterialApp(
      theme: AppTheme.patient(),
      home: Scaffold(
        body: StatefulBuilder(
          builder: (context, setState) => PhoneNumberField(
            country: country,
            controller: controller,
            onCountryChanged: (next) {
              setState(() => country = next);
              onChanged?.call(next);
            },
          ),
        ),
      ),
    ),
  );
  return controller;
}

void main() {
  testWidgets('deux champs : « Code » (+225 par défaut) et « Téléphone »',
      (tester) async {
    await _pump(tester);

    expect(find.text('Code'), findsOneWidget);
    expect(find.text('Téléphone'), findsOneWidget);
    expect(find.text('🇨🇮 +225'), findsOneWidget);
  });

  testWidgets('la saisie est affichée par paires : 05 44 21 01 12', (tester) async {
    final controller = await _pump(tester);

    await tester.enterText(find.byType(TextField), '0544210112');

    expect(controller.text, '05 44 21 01 12');
  });

  testWidgets("l'indicatif tapé ou collé dans le numéro est retiré", (tester) async {
    final controller = await _pump(tester);

    await tester.enterText(find.byType(TextField), '225 0544210112');
    expect(controller.text, '05 44 21 01 12');

    await tester.enterText(find.byType(TextField), '+225 05 44 21 01 12');
    expect(controller.text, '05 44 21 01 12');
  });

  testWidgets('les lettres sont ignorées et la longueur est bornée', (tester) async {
    final controller = await _pump(tester);

    await tester.enterText(find.byType(TextField), '05ab442101129999');

    expect(controller.text, '05 44 21 01 12');
  });

  testWidgets("effacer juste après un espace efface le chiffre qui le précède",
      (tester) async {
    final controller = await _pump(tester);
    await tester.enterText(find.byType(TextField), '0544');
    expect(controller.text, '05 44');

    // Curseur après l'espace (« 05 |44 ») puis retour arrière : le clavier
    // envoie « 0544 », curseur en 2 — l'espace seul a disparu.
    tester.testTextInput.updateEditingValue(const TextEditingValue(
      text: '0544',
      selection: TextSelection.collapsed(offset: 2),
    ));
    await tester.pump();

    expect(controller.text, '04 4');
    expect(controller.selection.baseOffset, 1);
  });

  testWidgets('choisir un autre pays notifie le parent et adapte l’exemple',
      (tester) async {
    PhoneCountry? chosen;
    await _pump(tester, onChanged: (c) => chosen = c);

    await tester.tap(find.text('🇨🇮 +225'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('🇸🇳 Sénégal  +221').last);
    await tester.pumpAndSettle();

    expect(chosen, PhoneCountries.senegal);
    expect(find.text('🇸🇳 +221'), findsOneWidget);
    expect(find.text(PhoneCountries.senegal.example), findsOneWidget);
  });
}
