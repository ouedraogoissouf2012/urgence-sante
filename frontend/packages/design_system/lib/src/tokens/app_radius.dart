import 'package:flutter/widgets.dart';

/// Tokens de rayons d'arrondi.
abstract final class AppRadius {
  static const Radius sm = Radius.circular(4);
  static const Radius md = Radius.circular(8);
  static const Radius lg = Radius.circular(16);

  static const BorderRadius card = BorderRadius.all(md);
  static const BorderRadius button = BorderRadius.all(lg);
}
