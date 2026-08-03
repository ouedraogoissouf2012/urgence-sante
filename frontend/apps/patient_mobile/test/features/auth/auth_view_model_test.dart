import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:patient_mobile/di/providers.dart';
import 'package:patient_mobile/features/auth/domain/auth_failure.dart';
import 'package:patient_mobile/features/auth/presentation/auth_state.dart';
import 'package:patient_mobile/features/auth/presentation/auth_view_model.dart';

import 'support/auth_fakes.dart';

void main() {
  late FakeAuthRepository repository;
  late InMemorySessionStore sessions;
  late ProviderContainer container;

  setUp(() {
    repository = FakeAuthRepository();
    sessions = InMemorySessionStore();
    container = ProviderContainer(
      overrides: [
        authRepositoryProvider.overrideWithValue(repository),
        sessionStoreProvider.overrideWithValue(sessions),
      ],
    );
    addTearDown(container.dispose);
  });

  AuthState state() => container.read(authViewModelProvider);
  AuthViewModel vm() => container.read(authViewModelProvider.notifier);

  test('inscription réussie enregistre le jeton et authentifie', () async {
    await vm().register(phone: '+2250102030405', password: 'MotDePasse2026');

    expect(state().phase, AuthPhase.authenticated);
    expect(await sessions.readToken(), 'jeton-de-test');
    expect(repository.lastPhone, '+2250102030405');
  });

  test('connexion réussie enregistre le jeton et authentifie', () async {
    await vm().login(phone: '+2250102030405', password: 'MotDePasse2026');

    expect(state().phase, AuthPhase.authenticated);
    expect(await sessions.readToken(), 'jeton-de-test');
  });

  test('numéro déjà utilisé : message dédié, pas de session', () async {
    repository.failWith = const AuthException(
        AuthFailure.phoneAlreadyUsed, 'Un compte existe déjà pour ce numéro.');

    await vm().register(phone: '+2250102030405', password: 'MotDePasse2026');

    expect(state().phase, AuthPhase.error);
    expect(state().errorMessage, contains('existe déjà'));
    expect(await sessions.readToken(), isNull);
  });

  test('identifiants invalides à la connexion : erreur, pas de session', () async {
    repository.failWith = const AuthException(
        AuthFailure.invalidCredentials, 'Numéro ou mot de passe incorrect.');

    await vm().login(phone: '+2250102030405', password: 'Faux');

    expect(state().phase, AuthPhase.error);
    expect(await sessions.readToken(), isNull);
  });

  test('une panne réseau produit une erreur lisible', () async {
    repository.failWith =
        const AuthException(AuthFailure.network, 'Service indisponible.');

    await vm().login(phone: '+2250102030405', password: 'MotDePasse2026');

    expect(state().phase, AuthPhase.error);
    expect(state().errorMessage, isNotNull);
  });
}
