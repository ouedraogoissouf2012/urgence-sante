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
  static const Color statusAvailable = Color(0xFF2E7D32);
  static const Color statusLimited = Color(0xFFF9A825);
  static const Color statusSaturated = Color(0xFFE65100);
  static const Color statusClosed = Color(0xFF757575);
  static const Color statusUnknown = Color(0xFF9E9E9E);

  /// Couleur du bouton d'appel d'urgence (contraste AA sur blanc).
  ///
  /// Volontairement plus foncée et intense que l'accent d'interface
  /// ([patientAccent]) : l'appel des secours (SAMU 185, Pompiers 180) est une
  /// action vitale qui doit se distinguer visuellement des autres boutons.
  static const Color emergencyCall = Color(0xFF8E0018);
}
