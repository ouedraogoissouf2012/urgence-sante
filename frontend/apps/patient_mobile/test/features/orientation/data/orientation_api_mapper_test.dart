import 'package:api_client/api.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/features/orientation/data/orientation_api_mapper.dart';

/// Le mapper API→domaine est testé INDÉPENDAMMENT de l'adaptateur et du réseau
/// (issue #47) : entrées du client généré, sorties du domaine.
void main() {
  test('un service du catalogue devient un besoin médical', () {
    final need = OrientationApiMapper.toNeed(
      MedicalService(code: 'maternity', label: 'Maternité', category: 'maternal'),
    );

    expect(need.code, 'maternity');
    expect(need.label, 'Maternité');
  });

  test('une recommandation devient un centre avec position, téléphone et qualité', () {
    final center = OrientationApiMapper.toCenter(
      Recommendation(
        facilityId: 'id-1',
        name: 'CHU de Cocody',
        location: GeoPoint(latitude: 5.3496, longitude: -3.9851),
        phone: '+2250100000001',
        distanceMeters: 2536.2,
        travelTimeSeconds: 292.4,
        travelTimeQuality: RecommendationTravelTimeQualityEnum.REAL,
        status: AvailabilityStatus.AVAILABLE,
        explanation: 'service disponible',
      ),
    );

    expect(center.facilityId, 'id-1');
    expect(center.latitude, 5.3496);
    expect(center.longitude, -3.9851);
    expect(center.phone, '+2250100000001');
    expect(center.travelTimeQuality, 'REAL');
    expect(center.status, 'AVAILABLE');
  });

  test('les champs optionnels absents restent nuls', () {
    final center = OrientationApiMapper.toCenter(
      Recommendation(
        facilityId: 'id-2',
        name: 'Centre sans téléphone',
        location: GeoPoint(latitude: 5.30, longitude: -4.00),
        distanceMeters: 1200.0,
        travelTimeQuality: RecommendationTravelTimeQualityEnum.ESTIMATED,
        status: AvailabilityStatus.UNKNOWN,
        explanation: 'disponibilité non confirmée',
      ),
    );

    expect(center.phone, isNull);
    expect(center.travelTimeSeconds, isNull);
    expect(center.travelTimeQuality, 'ESTIMATED');
  });

  test('les listes vides sont préservées', () {
    expect(OrientationApiMapper.toNeeds(const []), isEmpty);
    expect(OrientationApiMapper.toCenters(const []), isEmpty);
  });
}
