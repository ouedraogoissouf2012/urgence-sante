import 'package:app_foundation/app_foundation.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/app/patient_app.dart';

void main() {
  testWidgets("l'accueil affiche le titre et l'environnement", (tester) async {
    await tester.pumpWidget(
      const PatientApp(
        config: AppConfig(
          environment: AppEnvironment.development,
          apiBaseUrl: 'http://localhost:8080/api/v1',
        ),
      ),
    );

    expect(find.text('Urgence Santé'), findsWidgets);
    expect(find.textContaining('development'), findsOneWidget);
  });
}
