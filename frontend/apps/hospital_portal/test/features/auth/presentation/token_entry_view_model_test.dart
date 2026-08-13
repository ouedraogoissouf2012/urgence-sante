import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:hospital_portal/di/providers.dart';
import 'package:hospital_portal/features/auth/presentation/token_entry_view_model.dart';

import '../../../support/fake_token_store.dart';

void main() {
  late FakeTokenStore tokenStore;
  late ProviderContainer container;

  setUp(() {
    tokenStore = FakeTokenStore();
    container = ProviderContainer(overrides: [
      tokenStoreProvider.overrideWithValue(tokenStore),
    ]);
    addTearDown(container.dispose);
  });

  test('un jeton vide (ou blanc) est refusé sans toucher au stockage', () async {
    final viewModel = container.read(tokenEntryViewModelProvider.notifier);

    await viewModel.submit('   ');

    expect(await tokenStore.readToken(), isNull);
    expect(container.read(tokenEntryViewModelProvider).errorMessage, 'Le jeton est requis.');
  });

  test('un jeton non vide est nettoyé (trim) puis enregistré', () async {
    final viewModel = container.read(tokenEntryViewModelProvider.notifier);

    await viewModel.submit('  mon-jeton  ');

    expect(await tokenStore.readToken(), 'mon-jeton');
    expect(container.read(tokenEntryViewModelProvider).errorMessage, isNull);
  });

  test('un envoi valide invalide sessionTokenProvider pour faire basculer la porte',
      () async {
    // sessionTokenProvider lit tokenStoreProvider ; on le lit une première fois
    // (vide), on soumet un jeton, puis on vérifie que relire la session voit
    // bien la nouvelle valeur.
    expect(await container.read(sessionTokenProvider.future), isNull);

    await container.read(tokenEntryViewModelProvider.notifier).submit('mon-jeton');

    expect(await container.read(sessionTokenProvider.future), 'mon-jeton');
  });

  test(
      'submitting revient à false après un envoi réussi (sinon la PROCHAINE ouverture de '
      "cet écran, après une déconnexion, resterait bloquée : tokenEntryViewModelProvider "
      "n'est pas .autoDispose, son état survit)", () async {
    await container.read(tokenEntryViewModelProvider.notifier).submit('mon-jeton');

    expect(container.read(tokenEntryViewModelProvider).submitting, isFalse);
  });

  test('un second envoi pendant qu\'un premier est encore en cours est ignoré '
      '(double-tap)', () async {
    final viewModel = container.read(tokenEntryViewModelProvider.notifier);

    final first = viewModel.submit('premier-jeton');
    // Le premier envoi est encore en vol (submitting=true) : celui-ci doit
    // être un no-op immédiat, pas une seconde écriture concurrente.
    await viewModel.submit('second-jeton');
    await first;

    expect(await tokenStore.readToken(), 'premier-jeton');
  });
}
