/// Tokens d'élévation (profondeur des surfaces).
///
/// Échelle volontairement sobre : en contexte médical d'urgence, la profondeur
/// sert à hiérarchiser (qu'est-ce qui flotte au-dessus de quoi), pas à décorer.
/// Toutes les élévations des applications proviennent d'ici — aucune valeur
/// d'élévation brute dans les widgets.
abstract final class AppElevation {
  /// Surface posée à plat (pas d'ombre) : arrière-plans, conteneurs neutres.
  static const double flat = 0;

  /// Carte de contenu au repos : détachée du fond, ombre discrète.
  static const double card = 1;

  /// Carte mise en avant ou sélectionnée : ressort légèrement du lot.
  static const double cardSelected = 4;

  /// Feuille glissante (bottom sheet) : flotte nettement au-dessus de la carte.
  static const double sheet = 8;

  /// Bouton d'appel d'urgence : très légèrement surélevé pour appeler l'action
  /// sans dramatiser (l'urgence est déjà portée par la couleur et la taille).
  static const double emergency = 2;
}
