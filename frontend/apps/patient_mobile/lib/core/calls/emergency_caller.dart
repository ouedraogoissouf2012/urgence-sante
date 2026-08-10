import 'package:url_launcher/url_launcher.dart';

/// Levée quand le composeur téléphonique n'a pas pu être ouvert (aucune app de
/// téléphonie, intent refusé…). Sur une app d'urgence, un appel qui échoue ne
/// doit JAMAIS être silencieux : l'appelant informe l'utilisateur (issue #129).
class CallLaunchException implements Exception {
  const CallLaunchException(this.phoneNumber);

  final String phoneNumber;

  @override
  String toString() => 'CallLaunchException: impossible de composer $phoneNumber';
}

/// Contrat de déclenchement d'un appel téléphonique. Substituable en test.
///
/// Contrat d'erreur : en cas d'échec de lancement du composeur, l'implémentation
/// LÈVE (elle ne retourne jamais silencieusement). Les vues encapsulent l'appel
/// et signalent l'échec à l'utilisateur.
abstract interface class EmergencyCaller {
  Future<void> call(String phoneNumber);
}

/// Implémentation par le composeur du téléphone (tel:).
class DialerEmergencyCaller implements EmergencyCaller {
  const DialerEmergencyCaller();

  @override
  Future<void> call(String phoneNumber) async {
    final Uri uri = Uri(scheme: 'tel', path: phoneNumber);
    // launchUrl peut renvoyer false (composeur introuvable) OU lever une
    // PlatformException : les deux doivent remonter comme un échec explicite.
    final bool launched = await launchUrl(uri);
    if (!launched) {
      throw CallLaunchException(phoneNumber);
    }
  }
}
