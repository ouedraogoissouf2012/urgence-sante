import 'dart:async';

import 'package:flutter_test/flutter_test.dart';
import 'package:geolocator/geolocator.dart';
import 'package:patient_mobile/core/location/geolocator_location_service.dart';
import 'package:patient_mobile/core/location/location_service.dart';

void main() {
  group('GeolocatorLocationService.mapPositionException', () {
    test('un TimeoutException devient LocationFailure.timeout', () {
      final result = GeolocatorLocationService.mapPositionException(
        TimeoutException('trop long'),
      );

      expect(result.failure, LocationFailure.timeout);
      expect(result.message, isNot(contains('autorisation')));
    });

    test('un LocationServiceDisabledException devient serviceDisabled', () {
      final result = GeolocatorLocationService.mapPositionException(
        const LocationServiceDisabledException(),
      );

      expect(result.failure, LocationFailure.serviceDisabled);
    });

    test('un PermissionDeniedException devient denied', () {
      final result = GeolocatorLocationService.mapPositionException(
        const PermissionDeniedException('refusé'),
      );

      expect(result.failure, LocationFailure.denied);
    });

    test('une exception inattendue devient unknown, pas denied', () {
      final result = GeolocatorLocationService.mapPositionException(
        const FormatException('panne inattendue'),
      );

      expect(result.failure, LocationFailure.unknown);
      expect(result.message, isNot(contains('autorisation')));
    });

    test('trois causes distinctes produisent trois messages distincts', () {
      final timeout = GeolocatorLocationService.mapPositionException(
        TimeoutException('trop long'),
      );
      final disabled = GeolocatorLocationService.mapPositionException(
        const LocationServiceDisabledException(),
      );
      final denied = GeolocatorLocationService.mapPositionException(
        const PermissionDeniedException('refusé'),
      );

      expect({timeout.message, disabled.message, denied.message}, hasLength(3));
    });
  });
}
