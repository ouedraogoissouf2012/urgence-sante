//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;


class EmergencyTaxonomyApi {
  EmergencyTaxonomyApi([ApiClient? apiClient]) : apiClient = apiClient ?? defaultApiClient;

  final ApiClient apiClient;

  /// Obtenir la taxonomie des urgences (catégories et symptômes)
  ///
  /// Référentiel de navigation à deux niveaux (grande catégorie → symptôme). Chaque catégorie porte les codes des services médicaux « recherchés » et indique si elle relève d'un appel direct des secours (accidents, intoxications). 
  ///
  /// Note: This method returns the HTTP [Response].
  Future<Response> getEmergencyTaxonomyWithHttpInfo({ Future<void>? abortTrigger, }) async {
    // ignore: prefer_const_declarations
    final path = r'/emergency-taxonomy';

    // ignore: prefer_final_locals
    Object? postBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    const contentTypes = <String>[];


    return apiClient.invokeAPI(
      path,
      'GET',
      queryParams,
      postBody,
      headerParams,
      formParams,
      contentTypes.isEmpty ? null : contentTypes.first,
      abortTrigger: abortTrigger,
    );
  }

  /// Obtenir la taxonomie des urgences (catégories et symptômes)
  ///
  /// Référentiel de navigation à deux niveaux (grande catégorie → symptôme). Chaque catégorie porte les codes des services médicaux « recherchés » et indique si elle relève d'un appel direct des secours (accidents, intoxications). 
  Future<List<EmergencyCategory>?> getEmergencyTaxonomy({ Future<void>? abortTrigger, }) async {
    final response = await getEmergencyTaxonomyWithHttpInfo(abortTrigger: abortTrigger,);
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      final responseBody = await _decodeBodyBytes(response);
      return (await apiClient.deserializeAsync(responseBody, 'List<EmergencyCategory>') as List)
        .cast<EmergencyCategory>()
        .toList(growable: false);

    }
    return null;
  }
}
