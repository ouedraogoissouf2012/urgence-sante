import 'package:api_client/api.dart' as api;

import '../domain/model/emergency_category.dart';
import '../domain/repository/emergency_taxonomy_repository.dart';
import 'emergency_taxonomy_mapper.dart';

/// Adaptateur API de la taxonomie : appelle le client généré et délègue la
/// traduction au [EmergencyTaxonomyMapper].
class ApiEmergencyTaxonomyRepository implements EmergencyTaxonomyRepository {
  ApiEmergencyTaxonomyRepository(api.ApiClient apiClient)
      : _api = api.EmergencyTaxonomyApi(apiClient);

  final api.EmergencyTaxonomyApi _api;

  @override
  Future<List<EmergencyCategory>> fetchTaxonomy() async {
    final categories = await _api.getEmergencyTaxonomy();
    return EmergencyTaxonomyMapper.toDomain(categories ?? const []);
  }
}
