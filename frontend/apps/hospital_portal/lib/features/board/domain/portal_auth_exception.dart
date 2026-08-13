/// Raison précise d'un refus d'authentification/autorisation du portail.
enum PortalAuthFailure {
  /// Jeton absent, invalide ou révoqué (401) : permanent tant qu'un nouveau
  /// jeton n'est pas fourni — pas une panne réseau transitoire.
  unauthenticated,

  /// Jeton valide mais l'agent n'a pas la portée pour cet établissement (403).
  forbidden,
}

/// Levée par [PortalRepository] quand le serveur refuse une écriture pour une
/// raison d'authentification/autorisation, distincte d'une erreur réseau ou
/// serveur générique.
class PortalAuthException implements Exception {
  const PortalAuthException(this.failure);

  final PortalAuthFailure failure;

  @override
  String toString() => 'PortalAuthException($failure)';
}
