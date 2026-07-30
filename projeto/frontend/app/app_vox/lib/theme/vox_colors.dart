import 'package:flutter/material.dart';

/// Paleta extraída de site/vox/src/styles.scss e demais *.component.scss —
/// mantém o app com a mesma identidade visual do site VOX.
class VoxColors {
  static const primary = Color(0xFF1B3F8B);
  static const accent = Color(0xFF2D6FCC);
  static const secondary = Color(0xFFF5A800);
  static const background = Color(0xFFF0F4FF);
  static const textMuted = Color(0xFF6B7280);
  static const border = Color(0xFFE5E7EB);
  static const success = Color(0xFF15803D);
  static const successBg = Color(0xFFDCFCE7);
  static const error = Color(0xFFDC2626);
  static const errorBg = Color(0xFFFEE2E2);
  static const purple = Color(0xFF7C3AED);

  static const gradient = LinearGradient(
    begin: Alignment.centerLeft,
    end: Alignment.centerRight,
    colors: [primary, accent, secondary],
    stops: [0.0, 0.6, 1.0],
  );
}
