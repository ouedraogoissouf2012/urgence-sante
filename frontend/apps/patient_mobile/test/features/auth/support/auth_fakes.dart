import 'package:patient_mobile/core/auth/session_store.dart';
import 'package:patient_mobile/features/auth/domain/auth_failure.dart';
import 'package:patient_mobile/features/auth/domain/auth_repository.dart';

/// Faux dépôt d'authentification configurable, substituable au vrai (LSP).
class FakeAuthRepository implements AuthRepository {
  String token = 'jeton-de-test';
  AuthException? failWith;
  String? lastPhone;
  String? lastPassword;

  @override
  Future<String> register({required String phone, required String password}) async {
    lastPhone = phone;
    lastPassword = password;
    if (failWith != null) throw failWith!;
    return token;
  }

  @override
  Future<String> login({required String phone, required String password}) async {
    lastPhone = phone;
    lastPassword = password;
    if (failWith != null) throw failWith!;
    return token;
  }
}

/// Faux stockage de session en mémoire.
class InMemorySessionStore implements SessionStore {
  String? _token;

  @override
  Future<String?> readToken() async => _token;

  @override
  Future<void> saveToken(String token) async => _token = token;

  @override
  Future<void> clear() async => _token = null;
}
