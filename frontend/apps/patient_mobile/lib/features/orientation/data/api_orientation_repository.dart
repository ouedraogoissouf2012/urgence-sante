import 'package:api_client/api.dart';

import '../domain/model/medical_need.dart';
import '../domain/model/recommended_center.dart';
import 'orientation_api_mapper.dart';
import 'orientation_remote.dart';

/// Adaptateur API du parcours d'orientation : appelle le client généré et
/// délègue la traduction vers le domaine à [OrientationApiMapper] (testé
/// indépendamment). Le mode hors ligne est apporté par le décorateur de cache.
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
    return OrientationApiMapper.toNeeds(services);
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
    return OrientationApiMapper.toCenters(recommendations);
  }
}
