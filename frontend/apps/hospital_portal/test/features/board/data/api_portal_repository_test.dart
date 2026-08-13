import 'dart:convert';

import 'package:api_client/api.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:hospital_portal/features/board/data/api_portal_repository.dart';
import 'package:hospital_portal/features/board/domain/model/service_line.dart';
import 'package:hospital_portal/features/board/domain/portal_auth_exception.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

/// Exerce le VRAI `ApiPortalRepository` au-dessus d'un VRAI `ApiClient`, seule
/// la couche transport (`http.Client`) est remplacée — pas un mock du
/// ViewModel ni du repository. C'est le test que l'audit de l'issue #163
/// exige : les 3 suites existantes du portail substituent toutes
/// `portalRepositoryProvider`, court-circuitant ce chemin, ce qui a laissé le
/// bug (aucun jeton envoyé) passer inaperçu.
void main() {
  const line = ServiceLine(serviceCode: 'maternity', label: 'Maternité', status: 'UNKNOWN');

  http.Response okResponse() => http.Response(
        jsonEncode({
          'serviceCode': 'maternity',
          'status': 'LIMITED',
          'freshness': 'FRESH',
          'updatedAt': '2026-01-01T12:00:00Z',
        }),
        200,
        headers: {'content-type': 'application/json; charset=utf-8'},
      );

  test("envoie l'en-tête Authorization: Bearer <jeton> sur la mise à jour", () async {
    http.BaseRequest? captured;
    final apiClient = ApiClient(
      basePath: 'http://localhost:8080/api/v1',
      authentication: HttpBearerAuth()..accessToken = 'test-operator-token',
    )..client = MockClient((request) async {
        captured = request;
        return okResponse();
      });
    final repository = ApiPortalRepository(apiClient);

    await repository.updateStatus(facilityId: 'f-1', line: line, status: 'LIMITED');

    expect(captured, isNotNull);
    expect(captured!.headers['Authorization'], 'Bearer test-operator-token');
  });

  test('sans authentification configurée, aucun en-tête Authorization n\'est envoyé '
      '(reproduit le bug de l\'issue #163 : c\'est le comportement AVANT le correctif '
      'de di/providers.dart)', () async {
    http.BaseRequest? captured;
    final apiClient = ApiClient(basePath: 'http://localhost:8080/api/v1')
      ..client = MockClient((request) async {
        captured = request;
        return http.Response('', 401);
      });
    final repository = ApiPortalRepository(apiClient);

    await expectLater(
      repository.updateStatus(facilityId: 'f-1', line: line, status: 'LIMITED'),
      throwsA(isA<PortalAuthException>()),
    );
    expect(captured, isNotNull);
    expect(captured!.headers.containsKey('Authorization'), isFalse);
  });

  test('une réponse 401 lève PortalAuthException(unauthenticated), pas une erreur générique',
      () async {
    final apiClient = ApiClient(
      basePath: 'http://localhost:8080/api/v1',
      authentication: HttpBearerAuth()..accessToken = 'jeton-revoque',
    )..client = MockClient((request) async => http.Response('', 401));
    final repository = ApiPortalRepository(apiClient);

    await expectLater(
      repository.updateStatus(facilityId: 'f-1', line: line, status: 'LIMITED'),
      throwsA(
        isA<PortalAuthException>().having(
          (e) => e.failure,
          'failure',
          PortalAuthFailure.unauthenticated,
        ),
      ),
    );
  });

  test('une réponse 403 lève PortalAuthException(forbidden)', () async {
    final apiClient = ApiClient(
      basePath: 'http://localhost:8080/api/v1',
      authentication: HttpBearerAuth()..accessToken = 'jeton-hors-portee',
    )..client = MockClient((request) async => http.Response('', 403));
    final repository = ApiPortalRepository(apiClient);

    await expectLater(
      repository.updateStatus(facilityId: 'f-1', line: line, status: 'LIMITED'),
      throwsA(
        isA<PortalAuthException>().having(
          (e) => e.failure,
          'failure',
          PortalAuthFailure.forbidden,
        ),
      ),
    );
  });

  test('une réponse 500 reste une ApiException générique (pas un problème d\'authentification)',
      () async {
    final apiClient = ApiClient(
      basePath: 'http://localhost:8080/api/v1',
      authentication: HttpBearerAuth()..accessToken = 'test-operator-token',
    )..client = MockClient((request) async => http.Response('panne serveur', 500));
    final repository = ApiPortalRepository(apiClient);

    await expectLater(
      repository.updateStatus(facilityId: 'f-1', line: line, status: 'LIMITED'),
      throwsA(isA<ApiException>().having((e) => e.code, 'code', 500)),
    );
  });

  test('un succès met bien à jour le statut et l\'horodatage', () async {
    final apiClient = ApiClient(
      basePath: 'http://localhost:8080/api/v1',
      authentication: HttpBearerAuth()..accessToken = 'test-operator-token',
    )..client = MockClient((request) async => okResponse());
    final repository = ApiPortalRepository(apiClient);

    final updated = await repository.updateStatus(facilityId: 'f-1', line: line, status: 'LIMITED');

    expect(updated.status, 'LIMITED');
    expect(updated.freshness, 'FRESH');
    expect(updated.updatedAt, DateTime.utc(2026, 1, 1, 12));
  });
}
