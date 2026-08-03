import 'package:api_client/api.dart';

import '../domain/auth_failure.dart';
import '../domain/auth_repository.dart';

/// Implémentation de {@link AuthRepository} adossée au client API généré.
///
/// Traduit les codes HTTP du backend en {@link AuthException} typées : le
/// view-model ne dépend jamais du client généré (frontière propre).
class ApiAuthRepository implements AuthRepository {
  ApiAuthRepository(ApiClient apiClient) : _api = PatientsApi(apiClient);

  final PatientsApi _api;

  @override
  Future<String> register({required String phone, required String password}) {
    return _call(() => _api.registerPatient(
        RegisterPatientRequest(phone: phone, password: password)));
  }

  @override
  Future<String> login({required String phone, required String password}) {
    return _call(() => _api.loginPatient(LoginRequest(phone: phone, password: password)));
  }

  Future<String> _call(Future<PatientSession?> Function() action) async {
    try {
      final PatientSession? session = await action();
      final String? token = session?.token;
      if (token == null || token.isEmpty) {
        throw const AuthException(AuthFailure.network, 'Réponse inattendue du serveur.');
      }
      return token;
    } on ApiException catch (error) {
      throw _translate(error.code);
    } on AuthException {
      rethrow;
    } catch (_) {
      throw const AuthException(
          AuthFailure.network, 'Connexion au serveur impossible. Réessayez.');
    }
  }

  AuthException _translate(int httpCode) {
    return switch (httpCode) {
      400 => const AuthException(AuthFailure.invalidInput,
          'Vérifiez le numéro (format international) et le mot de passe (8 caractères minimum).'),
      401 => const AuthException(AuthFailure.invalidCredentials,
          'Numéro de téléphone ou mot de passe incorrect.'),
      409 => const AuthException(AuthFailure.phoneAlreadyUsed,
          'Un compte existe déjà pour ce numéro. Connectez-vous.'),
      429 => const AuthException(AuthFailure.tooManyAttempts,
          'Trop de tentatives. Patientez quelques instants.'),
      _ => const AuthException(AuthFailure.network, 'Service indisponible. Réessayez.'),
    };
  }
}
