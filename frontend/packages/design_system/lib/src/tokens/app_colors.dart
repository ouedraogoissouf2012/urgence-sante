import 'package:flutter/material.dart';

/// Tokens de couleurs du design system.
///
/// Toutes les couleurs des applications proviennent d'ici (aucune couleur codée
/// en dur dans les widgets applicatifs).
abstract final class AppColors {
  /// Amorce du thème patient : contexte d'urgence médicale.
  static const Color patientSeed = Color(0xFFB00020);

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
  static const Color emergencyCall = Color(0xFFB00020);
}
