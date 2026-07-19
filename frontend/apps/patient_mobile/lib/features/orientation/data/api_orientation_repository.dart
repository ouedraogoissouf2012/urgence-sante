import 'package:api_client/api.dart';

import '../domain/model/medical_need.dart';
import '../domain/model/recommended_center.dart';
import 'orientation_remote.dart';

/// Adaptateur API du parcours d'orientation : traduit le client généré en
/// modèles du domaine. Le mode hors ligne est apporté par le décorateur de
/// cache, sans logique réseau supplémentaire ici.
class ApiOrientationRepository implements OrientationRemote {
  ApiOrientationRepository(ApiClient apiClient)
      : _medicalServicesApi = MedicalServicesApi(apiClient),
        _orientationApi = OrientationApi(apiClient);

  final MedicalServicesApi _medicalServicesApi;
  final OrientationApi _orientationApi;

  @override
  Future<List<MedicalNeed>> loadNeeds() async {
    final List<MedicalService> services =
        await _medicalServicesApi.listMedicalServices() ?? const [];
    return services
        .map((service) => MedicalNeed(code: service.code, label: service.label))
        .toList();
  }

  @override
  Future<List<RecommendedCenter>> recommend({
    required double latitude,
    required double longitude,
    required String serviceCode,
  }) async {
    final List<Recommendation> recommendations = await _orientationApi
            .recommendFacilities(latitude, longitude, serviceCode) ??
        const [];
    return recommendations
        .map((r) => RecommendedCenter(
              facilityId: r.facilityId,
              name: r.name,
              latitude: r.location.latitude,
              longitude: r.location.longitude,
              phone: r.phone,
              distanceMeters: r.distanceMeters,
              travelTimeSeconds: r.travelTimeSeconds,
              travelTimeQuality: r.travelTimeQuality.value,
              status: r.status.value,
              explanation: r.explanation,
            ))
        .toList();
  }
}
