import 'package:url_launcher/url_launcher.dart';

/// Contrat de lancement d'un itinéraire vers une destination. Substituable en
/// test.
///
/// Décision (issue #39) : l'itinéraire guidé est délégué à l'application
/// cartographique de l'appareil (le tracé interne via la géométrie OSRM est
/// une évolution ultérieure).
abstract interface class NavigationLauncher {
  Future<void> navigateTo({
    required double latitude,
    required double longitude,
    required String label,
  });
}

/// Implémentation par application externe : `geo:` (Android) avec repli sur
/// Google Maps web si aucune application cartographique ne répond.
class ExternalMapNavigationLauncher implements NavigationLauncher {
  const ExternalMapNavigationLauncher();

  @override
  Future<void> navigateTo({
    required double latitude,
    required double longitude,
    required String label,
  }) async {
    final Uri geo = Uri.parse(
        'geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encodeComponent(label)})');
    if (await canLaunchUrl(geo)) {
      await launchUrl(geo);
      return;
    }
    final Uri web = Uri.parse(
        'https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude');
    // Repli web AUSSI gardé : sur un appareil sans handler (kiosque), lancer
    // sans vérifier lèverait une exception non gérée dans un chemin d'urgence.
    if (await canLaunchUrl(web)) {
      await launchUrl(web, mode: LaunchMode.externalApplication);
    }
  }
}
