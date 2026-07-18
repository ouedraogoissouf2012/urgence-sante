//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;


class RoutingApi {
  RoutingApi([ApiClient? apiClient]) : apiClient = apiClient ?? defaultApiClient;

  final ApiClient apiClient;

  /// Calculer un itinéraire entre deux points
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [double] fromLat (required):
  ///
  /// * [double] fromLon (required):
  ///
  /// * [double] toLat (required):
  ///
  /// * [double] toLon (required):
  Future<Response> getRouteWithHttpInfo(double fromLat, double fromLon, double toLat, double toLon, { Future<void>? abortTrigger, }) async {
    // ignore: prefer_const_declarations
    final path = r'/routes';

    // ignore: prefer_final_locals
    Object? postBody;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

      queryParams.addAll(_queryParams('', 'fromLat', fromLat));
      queryParams.addAll(_queryParams('', 'fromLon', fromLon));
      queryParams.addAll(_queryParams('', 'toLat', toLat));
      queryParams.addAll(_queryParams('', 'toLon', toLon));

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

  /// Calculer un itinéraire entre deux points
  ///
  /// Parameters:
  ///
  /// * [double] fromLat (required):
  ///
  /// * [double] fromLon (required):
  ///
  /// * [double] toLat (required):
  ///
  /// * [double] toLon (required):
  Future<Route?> getRoute(double fromLat, double fromLon, double toLat, double toLon, { Future<void>? abortTrigger, }) async {
    final response = await getRouteWithHttpInfo(fromLat, fromLon, toLat, toLon, abortTrigger: abortTrigger,);
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      return await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'Route',) as Route;
    
    }
    return null;
  }
}
