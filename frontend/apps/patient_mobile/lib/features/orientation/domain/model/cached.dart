/// Valeur potentiellement issue du cache local, avec sa provenance et sa date
/// de synchronisation (affichées à l'utilisateur en mode hors ligne).
class Cached<T> {
  const Cached.live(this.value)
      : fromCache = false,
        syncedAt = null;

  const Cached.fromStore(this.value, {required DateTime this.syncedAt})
      : fromCache = true;

  final T value;

  /// Vrai si la valeur provient du cache local (réseau indisponible).
  final bool fromCache;

  /// Date de la dernière synchronisation réussie (non nulle si [fromCache]).
  final DateTime? syncedAt;

  /// Vrai si la donnée en cache dépasse [maxAge] (affichage « périmé »).
  bool isStale(Duration maxAge, DateTime now) {
    final DateTime? synced = syncedAt;
    return synced != null && now.difference(synced) > maxAge;
  }
}
