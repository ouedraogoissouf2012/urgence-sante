//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;


class MedicalServicesApi {
  MedicalServicesApi([ApiClient? apiClient]) : apiClient = apiClient ?? defaultApiClient;

  final ApiClient apiClient;

  /// Lister le catalogue des services médicaux
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [String] category:
  ///   Filtre optionnel sur la catégorie d'un service.
  Future<Response> listMedicalServicesWithHttpInfo({ String? category, Future<void>? abortTrigger, }) async {
    // ignore: prefer_const_declarations
    final path = r'/medical-services';

    // ignore: prefer_final_locals
    Object? postBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    if (category != null) {
      queryParams.addAll(_queryParams('', 'category', category));
    }

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

  /// Lister le catalogue des services médicaux
  ///
  /// Parameters:
  ///
  /// * [String] category:
  ///   Filtre optionnel sur la catégorie d'un service.
  Future<List<MedicalService>?> listMedicalServices({ String? category, Future<void>? abortTrigger, }) async {
    final response = await listMedicalServicesWithHttpInfo(category: category, abortTrigger: abortTrigger,);
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      final responseBody = await _decodeBodyBytes(response);
      return (await apiClient.deserializeAsync(responseBody, 'List<MedicalService>') as List)
        .cast<MedicalService>()
        .toList(growable: false);

    }
    return null;
  }
}
