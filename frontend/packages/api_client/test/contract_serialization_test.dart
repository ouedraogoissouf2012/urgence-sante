import 'dart:convert';

import 'package:api_client/api.dart';
import 'package:flutter_test/flutter_test.dart';

/// Tests de désérialisation du client GÉNÉRÉ contre des charges utiles
/// représentatives du contrat. Ils protègent la génération : un client
/// régénéré incapable de lire les réponses du backend échoue ici.
void main() {
  test('Recommendation se désérialise depuis une réponse du contrat', () {
    const payload = '''
    {
      "facilityId": "11111111-0000-0000-0000-000000000001",
      "name": "CHU de Cocody",
      "location": {"latitude": 5.3496, "longitude": -3.9851},
      "phone": "+2250100000001",
      "distanceMeters": 2536.2,
      "travelTimeSeconds": 292.4,
      "status": "AVAILABLE",
      "explanation": "service disponible"
    }''';

    final recommendation = Recommendation.fromJson(jsonDecode(payload))!;

    expect(recommendation.name, 'CHU de Cocody');
    expect(recommendation.location.latitude, 5.3496);
    expect(recommendation.phone, '+2250100000001');
    expect(recommendation.status, AvailabilityStatus.AVAILABLE);
  });

  test('le téléphone absent reste nul (donnée optionnelle)', () {
    const payload = '''
    {
      "facilityId": "11111111-0000-0000-0000-000000000002",
      "name": "Centre sans téléphone",
      "location": {"latitude": 5.30, "longitude": -4.00},
      "distanceMeters": 1200.0,
      "status": "UNKNOWN",
      "explanation": "disponibilité non confirmée"
    }''';

    final recommendation = Recommendation.fromJson(jsonDecode(payload))!;

    expect(recommendation.phone, isNull);
    expect(recommendation.travelTimeSeconds, isNull);
  });

  test('Problem (RFC 9457) se désérialise', () {
    const payload = '''
    {
      "type": "about:blank",
      "title": "Requête invalide",
      "status": 400,
      "detail": "Latitude hors bornes"
    }''';

    final problem = Problem.fromJson(jsonDecode(payload))!;

    expect(problem.title, 'Requête invalide');
    expect(problem.status, 400);
  });

  test('FacilityAvailability et son historique se désérialisent', () {
    const payload = '''
    {
      "facilityId": "11111111-0000-0000-0000-000000000001",
      "services": [
        {"serviceCode": "maternity", "status": "SATURATED",
         "freshness": "FRESH", "updatedAt": "2026-07-18T10:00:00Z"}
      ]
    }''';

    final availability = FacilityAvailability.fromJson(jsonDecode(payload))!;

    expect(availability.services.single.status, AvailabilityStatus.SATURATED);
    expect(availability.services.single.freshness, Freshness.FRESH);
  });
}
