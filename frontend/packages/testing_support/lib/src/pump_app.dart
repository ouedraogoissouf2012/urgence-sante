import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

/// Utilitaires de test partagés par les applications.
extension PumpApp on WidgetTester {
  /// Monte [widget] dans un contexte minimal (direction de texte fournie),
  /// pour tester un composant isolé sans démarrer une application complète.
  Future<void> pumpIsolated(Widget widget) {
    return pumpWidget(
      Directionality(
        textDirection: TextDirection.ltr,
        child: widget,
      ),
    );
  }
}
