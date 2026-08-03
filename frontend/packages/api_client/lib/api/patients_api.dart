//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element, unused_import
// ignore_for_file: always_put_required_named_parameters_first
// ignore_for_file: constant_identifier_names
// ignore_for_file: lines_longer_than_80_chars

part of openapi.api;


class PatientsApi {
  PatientsApi([ApiClient? apiClient]) : apiClient = apiClient ?? defaultApiClient;

  final ApiClient apiClient;

  /// Connexion d'un patient
  ///
  /// Vérifie les identifiants et renvoie un jeton porteur de session. Message d'erreur volontairement générique (ne révèle pas l'existence d'un compte).
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [LoginRequest] loginRequest (required):
  Future<Response> loginPatientWithHttpInfo(LoginRequest loginRequest, { Future<void>? abortTrigger, }) async {
    // ignore: prefer_const_declarations
    final path = r'/patients/session';

    // ignore: prefer_final_locals
    Object? postBody = loginRequest;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    const contentTypes = <String>['application/json'];


    return apiClient.invokeAPI(
      path,
      'POST',
      queryParams,
      postBody,
      headerParams,
      formParams,
      contentTypes.isEmpty ? null : contentTypes.first,
      abortTrigger: abortTrigger,
    );
  }

  /// Connexion d'un patient
  ///
  /// Vérifie les identifiants et renvoie un jeton porteur de session. Message d'erreur volontairement générique (ne révèle pas l'existence d'un compte).
  ///
  /// Parameters:
  ///
  /// * [LoginRequest] loginRequest (required):
  Future<PatientSession?> loginPatient(LoginRequest loginRequest, { Future<void>? abortTrigger, }) async {
    final response = await loginPatientWithHttpInfo(loginRequest, abortTrigger: abortTrigger,);
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      return await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'PatientSession',) as PatientSession;
    
    }
    return null;
  }

  /// Inscription d'un patient (téléphone + mot de passe)
  ///
  /// Crée un compte patient et ouvre immédiatement une session. Le mot de passe n'est jamais stocké en clair (empreinte BCrypt). Aucune donnée médicale n'est collectée ni transmise : elle reste sur l'appareil.
  ///
  /// Note: This method returns the HTTP [Response].
  ///
  /// Parameters:
  ///
  /// * [RegisterPatientRequest] registerPatientRequest (required):
  Future<Response> registerPatientWithHttpInfo(RegisterPatientRequest registerPatientRequest, { Future<void>? abortTrigger, }) async {
    // ignore: prefer_const_declarations
    final path = r'/patients';

    // ignore: prefer_final_locals
    Object? postBody = registerPatientRequest;

    final queryParams = <QueryParam>[];
    final headerParams = <String, String>{};
    final formParams = <String, String>{};

    const contentTypes = <String>['application/json'];


    return apiClient.invokeAPI(
      path,
      'POST',
      queryParams,
      postBody,
      headerParams,
      formParams,
      contentTypes.isEmpty ? null : contentTypes.first,
      abortTrigger: abortTrigger,
    );
  }

  /// Inscription d'un patient (téléphone + mot de passe)
  ///
  /// Crée un compte patient et ouvre immédiatement une session. Le mot de passe n'est jamais stocké en clair (empreinte BCrypt). Aucune donnée médicale n'est collectée ni transmise : elle reste sur l'appareil.
  ///
  /// Parameters:
  ///
  /// * [RegisterPatientRequest] registerPatientRequest (required):
  Future<PatientSession?> registerPatient(RegisterPatientRequest registerPatientRequest, { Future<void>? abortTrigger, }) async {
    final response = await registerPatientWithHttpInfo(registerPatientRequest, abortTrigger: abortTrigger,);
    if (response.statusCode >= HttpStatus.badRequest) {
      throw ApiException(response.statusCode, await _decodeBodyBytes(response));
    }
    // When a remote server returns no body with a status of 204, we shall not decode it.
    // At the time of writing this, `dart:convert` will throw an "Unexpected end of input"
    // FormatException when trying to decode an empty string.
    if (response.body.isNotEmpty && response.statusCode != HttpStatus.noContent) {
      return await apiClient.deserializeAsync(await _decodeBodyBytes(response), 'PatientSession',) as PatientSession;
    
    }
    return null;
  }
}
