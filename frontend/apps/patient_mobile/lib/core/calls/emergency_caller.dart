import 'package:url_launcher/url_launcher.dart';

/// Contrat de déclenchement d'un appel téléphonique. Substituable en test.
abstract interface class EmergencyCaller {
  Future<void> call(String phoneNumber);
}

/// Implémentation par le composeur du téléphone (tel:).
class DialerEmergencyCaller implements EmergencyCaller {
  const DialerEmergencyCaller();

  @override
  Future<void> call(String phoneNumber) async {
    final Uri uri = Uri(scheme: 'tel', path: phoneNumber);
    await launchUrl(uri);
  }
}
