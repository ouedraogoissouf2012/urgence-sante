import 'package:design_system/design_system.dart';
import 'package:flutter/material.dart';
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

  test('le thème patient impose le corail vif de marque', () {
    expect(AppTheme.patient().colorScheme.primary, AppColors.patientAccent);
  });

  test('le thème sombre partage l\'accent et inverse la luminosité', () {
    final dark = AppTheme.patientDark();
    expect(dark.colorScheme.brightness, Brightness.dark);
    expect(dark.colorScheme.primary, AppColors.patientAccent);
  });

  test('le thème light reste disponible (compatibilité)', () {
    expect(
      AppTheme.light().colorScheme.primary,
      AppTheme.patient().colorScheme.primary,
    );
  });
}
