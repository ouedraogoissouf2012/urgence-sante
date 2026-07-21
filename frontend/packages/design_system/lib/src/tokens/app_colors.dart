import 'package:flutter/material.dart';

/// Tokens de couleurs du design system.
///
/// Toutes les couleurs des applications proviennent d'ici (aucune couleur codée
/// en dur dans les widgets applicatifs).
abstract final class AppColors {
  /// Amorce du thème patient : corail médical.
  ///
  /// Changer cette seule valeur repeint tout le thème patient (couleur
  /// primaire, bordures de sélection, boutons remplis) via
  /// [ColorScheme.fromSeed] dans `AppTheme`. C'est le point de contrôle
  /// central de l'identité visuelle de l'application patient.
  static const Color patientSeed = Color(0xFFE8483F);

  /// Rouge médical franc, imposé comme couleur primaire du thème patient.
  ///
  /// [ColorScheme.fromSeed] harmonise (et atténue) la couleur d'amorce ; on
  /// force cette valeur exacte pour que les boutons gardent un rouge
  /// institutionnel net, tout en conservant la palette harmonisée pour le reste.
  static const Color patientAccent = Color(0xFFB00020);

  /// Amorce du thème hôpital : contexte institutionnel.
  static const Color hospitalSeed = Color(0xFF00579B);

  // ── Couleurs sémantiques de disponibilité ────────────────────────────────
  // Toujours accompagnées d'un libellé texte : la couleur seule ne porte
  // jamais l'information (accessibilité).
  //
  // Trois rôles distincts par statut :
  //  • la teinte d'identité (`status…`) — fond translucide + bordure du badge,
  //    reconnaissable d'un coup d'œil, identique dans les deux thèmes ;
  //  • la couleur de texte claire (`status…TextLight`) — assombrie, lisible sur
  //    le fond clair du badge ;
  //  • la couleur de texte sombre (`status…TextDark`) — éclaircie, lisible sur
  //    le fond sombre du badge.
  //
  // Une seule couleur de texte ne peut pas convenir aux deux thèmes : mesuré
  // sur les surfaces réelles Material 3 (fond clair #FFF8F7, fond sombre
  // #1A1110), un texte foncé illisible en sombre et inversement. Le badge
  // choisit la variante selon la luminosité du thème. Toutes atteignent
  // ≥ 4.5:1 (AA texte normal) sur le fond réel du badge — voir
  // `status_badge_test.dart` (garde des deux thèmes).
  static const Color statusAvailable = Color(0xFF2E7D32);
  static const Color statusAvailableTextLight = Color(0xFF1B5E20);
  static const Color statusAvailableTextDark = Color(0xFF81C784);
  static const Color statusLimited = Color(0xFFF9A825);
  static const Color statusLimitedTextLight = Color(0xFF7A5000);
  static const Color statusLimitedTextDark = Color(0xFFFFCA28);
  static const Color statusSaturated = Color(0xFFE65100);
  static const Color statusSaturatedTextLight = Color(0xFFA93D00);
  static const Color statusSaturatedTextDark = Color(0xFFFFA726);
  static const Color statusClosed = Color(0xFF757575);
  static const Color statusClosedTextLight = Color(0xFF5A5A5A);
  static const Color statusClosedTextDark = Color(0xFFBDBDBD);
  static const Color statusUnknown = Color(0xFF9E9E9E);
  static const Color statusUnknownTextLight = Color(0xFF575757);
  static const Color statusUnknownTextDark = Color(0xFFBDBDBD);

  /// Couleur du bouton d'appel d'urgence (contraste AA sur blanc).
  ///
  /// Volontairement plus foncée et intense que l'accent d'interface
  /// ([patientAccent]) : l'appel des secours (SAMU 185, Pompiers 180) est une
  /// action vitale qui doit se distinguer visuellement des autres boutons.
  static const Color emergencyCall = Color(0xFF8E0018);
}
