import 'package:api_client/api.dart';

import '../domain/model/recommended_center.dart';
import 'orientation_api_mapper.dart';
import 'orientation_remote.dart';

/// Adaptateur API du parcours d'orientation : appelle le client généré et
/// délègue la traduction vers le domaine à [OrientationApiMapper] (testé
/// indépendamment). Le mode hors ligne est apporté par le décorateur de cache.
class ApiOrientationRepository implements OrientationRemote {
  ApiOrientationRepository(ApiClient apiClient)
      : _orientationApi = OrientationApi(apiClient);

  final OrientationApi _orientationApi;

  @override
  Future<List<RecommendedCenter>> recommend({
    required double latitude,
    required double longitude,
    required List<String> serviceCodes,
  }) async {
    final List<Recommendation> recommendations =
        await _orientationApi.recommendFacilities(
              latitude,
              longitude,
              services: serviceCodes,
            ) ??
            const [];
    return OrientationApiMapper.toCenters(recommendations);
  }
}
