//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;


class OrientationApi {
  OrientationApi([ApiClient? apiClient]) : apiClient = apiClient ?? defaultApiClient;

  final ApiClient apiClient;

  /// Recommander les centres adaptés pour un besoin médical
  ///
  /// Classe les établissements offrant le service demandé selon la proximité, la disponibilité (un statut périmé est traité comme non confirmé) et le temps de trajet, avec une explication lisible.
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [double] lat (required):
  ///
  /// * [double] lon (required):
  ///
  /// * [String] service (required):
  ///   Code du service médical recherché (catalogue).
  ///
  /// * [int] radiusMeters:
  ///
  /// * [int] limit:
  Future<Response> recommendFacilitiesWithHttpInfo(double lat, double lon, String service, { int? radiusMeters, int? limit, Future<void>? abortTrigger, }) async {
    // ignore: prefer_const_declarations
    final path = r'/orientation';

    // ignore: prefer_final_locals
    Object? postBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

      queryParams.addAll(_queryParams('', 'lat', lat));
      queryParams.addAll(_queryParams('', 'lon', lon));
      queryParams.addAll(_queryParams('', 'service', service));
    if (radiusMeters != null) {
      queryParams.addAll(_queryParams('', 'radiusMeters', radiusMeters));
    }
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

  /// Recommander les centres adaptés pour un besoin médical
  ///
  /// Classe les établissements offrant le service demandé selon la proximité, la disponibilité (un statut périmé est traité comme non confirmé) et le temps de trajet, avec une explication lisible.
  ///
  /// Parameters:
  ///
  /// * [double] lat (required):
  ///
  /// * [double] lon (required):
  ///
  /// * [String] service (required):
  ///   Code du service médical recherché (catalogue).
  ///
  /// * [int] radiusMeters:
  ///
  /// * [int] limit:
  Future<List<Recommendation>?> recommendFacilities(double lat, double lon, String service, { int? radiusMeters, int? limit, Future<void>? abortTrigger, }) async {
    final response = await recommendFacilitiesWithHttpInfo(lat, lon, service, radiusMeters: radiusMeters, limit: limit, abortTrigger: abortTrigger,);
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      final responseBody = await _decodeBodyBytes(response);
      return (await apiClient.deserializeAsync(responseBody, 'List<Recommendation>') as List)
        .cast<Recommendation>()
        .toList(growable: false);

    }
    return null;
  }
}
