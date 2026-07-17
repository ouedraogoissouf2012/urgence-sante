import 'package:app_foundation/app_foundation.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:hospital_portal/app/portal_app.dart';

void main() {
  testWidgets("l'accueil affiche le titre du portail", (tester) async {
    await tester.pumpWidget(
      const PortalApp(
        config: AppConfig(
          environment: AppEnvironment.development,
          apiBaseUrl: 'http://localhost:8080/api/v1',
        ),
      ),
    );

    expect(find.text('Portail hospitalier'), findsWidgets);
    expect(find.textContaining('development'), findsOneWidget);
  });
}
