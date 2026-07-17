//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;


class FacilitiesApi {
  FacilitiesApi([ApiClient? apiClient]) : apiClient = apiClient ?? defaultApiClient;

  final ApiClient apiClient;

  /// Consulter un établissement
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [String] facilityId (required):
  ///   Identifiant unique de l'établissement.
  Future<Response> getFacilityWithHttpInfo(String facilityId, { Future<void>? abortTrigger, }) async {
    // ignore: prefer_const_declarations
    final path = r'/facilities/{facilityId}'
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

  /// Consulter un établissement
  ///
  /// Parameters:
  ///
  /// * [String] facilityId (required):
  ///   Identifiant unique de l'établissement.
  Future<Facility?> getFacility(String facilityId, { Future<void>? abortTrigger, }) async {
    final response = await getFacilityWithHttpInfo(facilityId, abortTrigger: abortTrigger,);
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      return await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'Facility',) as Facility;
    
    }
    return null;
  }

  /// Lister les établissements de santé
  ///
  /// Retourne une page d'établissements. Peut être filtrée par service médical et triée par proximité si une position est fournie. 
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [int] page:
  ///   Index de page, à partir de 0.
  ///
  /// * [int] size:
  ///   Taille de page (nombre d'éléments).
  ///
  /// * [String] service:
  ///   Filtre sur le code d'un service médical (ex. « maternity »).
  ///
  /// * [double] lat:
  ///   Latitude du point de recherche (WGS84). Requiert « lon ».
  ///
  /// * [double] lon:
  ///   Longitude du point de recherche (WGS84). Requiert « lat ».
  ///
  /// * [int] radiusMeters:
  ///   Rayon de recherche en mètres autour du point fourni.
  Future<Response> listFacilitiesWithHttpInfo({ int? page, int? size, String? service, double? lat, double? lon, int? radiusMeters, Future<void>? abortTrigger, }) async {
    // ignore: prefer_const_declarations
    final path = r'/facilities';

    // ignore: prefer_final_locals
    Object? postBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    if (page != null) {
      queryParams.addAll(_queryParams('', 'page', page));
    }
    if (size != null) {
      queryParams.addAll(_queryParams('', 'size', size));
    }
    if (service != null) {
      queryParams.addAll(_queryParams('', 'service', service));
    }
    if (lat != null) {
      queryParams.addAll(_queryParams('', 'lat', lat));
    }
    if (lon != null) {
      queryParams.addAll(_queryParams('', 'lon', lon));
    }
    if (radiusMeters != null) {
      queryParams.addAll(_queryParams('', 'radiusMeters', radiusMeters));
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

  /// Lister les établissements de santé
  ///
  /// Retourne une page d'établissements. Peut être filtrée par service médical et triée par proximité si une position est fournie. 
  ///
  /// Parameters:
  ///
  /// * [int] page:
  ///   Index de page, à partir de 0.
  ///
  /// * [int] size:
  ///   Taille de page (nombre d'éléments).
  ///
  /// * [String] service:
  ///   Filtre sur le code d'un service médical (ex. « maternity »).
  ///
  /// * [double] lat:
  ///   Latitude du point de recherche (WGS84). Requiert « lon ».
  ///
  /// * [double] lon:
  ///   Longitude du point de recherche (WGS84). Requiert « lat ».
  ///
  /// * [int] radiusMeters:
  ///   Rayon de recherche en mètres autour du point fourni.
  Future<PagedFacilities?> listFacilities({ int? page, int? size, String? service, double? lat, double? lon, int? radiusMeters, Future<void>? abortTrigger, }) async {
    final response = await listFacilitiesWithHttpInfo(page: page, size: size, service: service, lat: lat, lon: lon, radiusMeters: radiusMeters, abortTrigger: abortTrigger,);
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      return await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'PagedFacilities',) as PagedFacilities;
    
    }
    return null;
  }
}
