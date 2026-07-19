//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;


class AvailabilityApi {
  AvailabilityApi([ApiClient? apiClient]) : apiClient = apiClient ?? defaultApiClient;

  final ApiClient apiClient;

  /// Historique des mises à jour de statut d'un service
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [String] facilityId (required):
  ///   Identifiant unique de l'établissement.
  ///
  /// * [String] serviceCode (required):
  ///
  /// * [int] limit:
  Future<Response> getAvailabilityHistoryWithHttpInfo(String facilityId, String serviceCode, { int? limit, Future<void>? abortTrigger, }) async {
    // ignore: prefer_const_declarations
    final path = r'/facilities/{facilityId}/availability/{serviceCode}/history'
      .replaceAll('{facilityId}', facilityId)
      .replaceAll('{serviceCode}', serviceCode);

    // ignore: prefer_final_locals
    Object? postBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    if (limit != null) {
      queryParams.addAll(_queryParams('', 'limit', limit));
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

  /// Historique des mises à jour de statut d'un service
  ///
  /// Parameters:
  ///
  /// * [String] facilityId (required):
  ///   Identifiant unique de l'établissement.
  ///
  /// * [String] serviceCode (required):
  ///
  /// * [int] limit:
  Future<List<AvailabilityHistoryEntry>?> getAvailabilityHistory(String facilityId, String serviceCode, { int? limit, Future<void>? abortTrigger, }) async {
    final response = await getAvailabilityHistoryWithHttpInfo(facilityId, serviceCode, limit: limit, abortTrigger: abortTrigger,);
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      final responseBody = await _decodeBodyBytes(response);
      return (await apiClient.deserializeAsync(responseBody, 'List<AvailabilityHistoryEntry>') as List)
        .cast<AvailabilityHistoryEntry>()
        .toList(growable: false);

    }
    return null;
  }

  /// Consulter la disponibilité des services d'un établissement
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [String] facilityId (required):
  ///   Identifiant unique de l'établissement.
  Future<Response> getFacilityAvailabilityWithHttpInfo(String facilityId, { Future<void>? abortTrigger, }) async {
    // ignore: prefer_const_declarations
    final path = r'/facilities/{facilityId}/availability'
      .replaceAll('{facilityId}', facilityId);

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

  /// Consulter la disponibilité des services d'un établissement
  ///
  /// Parameters:
  ///
  /// * [String] facilityId (required):
  ///   Identifiant unique de l'établissement.
  Future<FacilityAvailability?> getFacilityAvailability(String facilityId, { Future<void>? abortTrigger, }) async {
    final response = await getFacilityAvailabilityWithHttpInfo(facilityId, abortTrigger: abortTrigger,);
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      return await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'FacilityAvailability',) as FacilityAvailability;
    
    }
    return null;
  }

  /// Mettre à jour le statut d'un service (agent hospitalier)
  ///
  /// Réservé au portail hospitalier authentifié. Nécessite un jeton porteur (rôle opérateur de l'établissement visé, ou administrateur). Débit limité.
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [String] facilityId (required):
  ///   Identifiant unique de l'établissement.
  ///
  /// * [String] serviceCode (required):
  ///
  /// * [UpdateAvailabilityRequest] updateAvailabilityRequest (required):
  Future<Response> updateAvailabilityWithHttpInfo(String facilityId, String serviceCode, UpdateAvailabilityRequest updateAvailabilityRequest, { Future<void>? abortTrigger, }) async {
    // ignore: prefer_const_declarations
    final path = r'/facilities/{facilityId}/availability/{serviceCode}'
      .replaceAll('{facilityId}', facilityId)
      .replaceAll('{serviceCode}', serviceCode);

    // ignore: prefer_final_locals
    Object? postBody = updateAvailabilityRequest;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    const contentTypes = <String>['application/json'];


    return apiClient.invokeAPI(
      path,
      'PUT',
      queryParams,
      postBody,
      headerParams,
      formParams,
      contentTypes.isEmpty ? null : contentTypes.first,
      abortTrigger: abortTrigger,
    );
  }

  /// Mettre à jour le statut d'un service (agent hospitalier)
  ///
  /// Réservé au portail hospitalier authentifié. Nécessite un jeton porteur (rôle opérateur de l'établissement visé, ou administrateur). Débit limité.
  ///
  /// Parameters:
  ///
  /// * [String] facilityId (required):
  ///   Identifiant unique de l'établissement.
  ///
  /// * [String] serviceCode (required):
  ///
  /// * [UpdateAvailabilityRequest] updateAvailabilityRequest (required):
  Future<ServiceAvailability?> updateAvailability(String facilityId, String serviceCode, UpdateAvailabilityRequest updateAvailabilityRequest, { Future<void>? abortTrigger, }) async {
    final response = await updateAvailabilityWithHttpInfo(facilityId, serviceCode, updateAvailabilityRequest, abortTrigger: abortTrigger,);
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      return await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'ServiceAvailability',) as ServiceAvailability;
    
    }
    return null;
  }
}
