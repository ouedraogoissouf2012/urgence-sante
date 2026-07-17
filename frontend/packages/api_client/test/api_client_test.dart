import 'package:api_client/api_client.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('la version du contrat ciblé est v1', () {
    expect(apiContractVersion, 'v1');
  });
}
