import 'package:api_client/api.dart';

import '../domain/model/medical_need.dart';
import '../domain/model/recommended_center.dart';
import '../domain/repository/orientation_repository.dart';

/// Adaptateur API du parcours d'orientation : traduit le client généré en
/// modèles du domaine de l'application. Aucune logique métier.
class ApiOrientationRepository implements OrientationRepository {
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
              status: r.status.value,
              explanation: r.explanation,
            ))
        .toList();
  }
}
