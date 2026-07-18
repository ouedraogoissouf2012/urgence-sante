import 'package:design_system/design_system.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('les thèmes patient et hôpital sont Material 3 et distincts', () {
    final patient = AppTheme.patient();
    final hospital = AppTheme.hospital();

    expect(patient.useMaterial3, isTrue);
    expect(hospital.useMaterial3, isTrue);
    expect(
      patient.colorScheme.primary,
      isNot(equals(hospital.colorScheme.primary)),
    );
  });

  test('le thème light reste disponible (compatibilité)', () {
    expect(
      AppTheme.light().colorScheme.primary,
      AppTheme.patient().colorScheme.primary,
    );
  });
}
