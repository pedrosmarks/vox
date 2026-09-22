import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import '../services/settings_service.dart';
import 'vox_colors.dart';

/// Tema global do app, equivalente ao styles.scss do site (mesmas cores,
/// tipografia Inter e cantos arredondados usados nos cards/botões).
class VoxTheme {
  /// Tema a ser usado conforme o modo de acessibilidade selecionado.
  /// (Os modos de daltonismo usam o tema claro; o filtro de cor é aplicado
  /// globalmente pelo ColorFiltered em main.dart.)
  static ThemeData forMode(AccessibilityMode mode) {
    switch (mode) {
      case AccessibilityMode.dark:
        return dark;
      case AccessibilityMode.highContrast:
        return highContrast;
      default:
        return light;
    }
  }

  static ThemeData get light {
    final base = ThemeData(
      useMaterial3: true,
      colorScheme: ColorScheme.fromSeed(
        seedColor: VoxColors.primary,
        primary: VoxColors.primary,
        secondary: VoxColors.secondary,
        error: VoxColors.error,
        surface: Colors.white,
      ),
      scaffoldBackgroundColor: VoxColors.background,
      fontFamily: GoogleFonts.inter().fontFamily,
      textTheme: GoogleFonts.interTextTheme(),
    );

    return base.copyWith(
      appBarTheme: const AppBarTheme(
        backgroundColor: VoxColors.primary,
        foregroundColor: Colors.white,
        elevation: 0,
        centerTitle: false,
        titleTextStyle: TextStyle(
          color: Colors.white,
          fontSize: 20,
          fontWeight: FontWeight.w700,
        ),
      ),
      cardTheme: CardThemeData(
        color: Colors.white,
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12),
          side: const BorderSide(color: VoxColors.border),
        ),
        margin: EdgeInsets.zero,
      ),
      chipTheme: base.chipTheme.copyWith(
        backgroundColor: VoxColors.background,
        labelStyle: const TextStyle(
          fontSize: 12,
          fontWeight: FontWeight.w600,
          color: VoxColors.primary,
        ),
        shape: const StadiumBorder(),
        side: BorderSide.none,
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: const Color(0xFFFAFBFF),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(10),
          borderSide: const BorderSide(color: VoxColors.border, width: 1.5),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(10),
          borderSide: const BorderSide(color: VoxColors.border, width: 1.5),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(10),
          borderSide: const BorderSide(color: VoxColors.accent, width: 2),
        ),
        labelStyle: const TextStyle(
          color: VoxColors.textMuted,
          fontWeight: FontWeight.w600,
        ),
        contentPadding: const EdgeInsets.symmetric(
          horizontal: 14,
          vertical: 12,
        ),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          backgroundColor: VoxColors.primary,
          foregroundColor: Colors.white,
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(10),
          ),
          textStyle: const TextStyle(fontWeight: FontWeight.w700, fontSize: 15),
        ),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: VoxColors.primary,
          foregroundColor: Colors.white,
          elevation: 0,
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(10),
          ),
          textStyle: const TextStyle(fontWeight: FontWeight.w700, fontSize: 15),
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          foregroundColor: VoxColors.primary,
          side: const BorderSide(color: VoxColors.border, width: 1.5),
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(10),
          ),
          textStyle: const TextStyle(fontWeight: FontWeight.w600, fontSize: 15),
        ),
      ),
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(
          foregroundColor: VoxColors.primary,
          textStyle: const TextStyle(fontWeight: FontWeight.w600),
        ),
      ),
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: Colors.white,
        indicatorColor: VoxColors.primary.withValues(alpha: 0.12),
        labelTextStyle: WidgetStateProperty.resolveWith((states) {
          final selected = states.contains(WidgetState.selected);
          return TextStyle(
            fontSize: 11,
            fontWeight: selected ? FontWeight.w700 : FontWeight.w500,
            color: selected ? VoxColors.primary : VoxColors.textMuted,
          );
        }),
        iconTheme: WidgetStateProperty.resolveWith((states) {
          final selected = states.contains(WidgetState.selected);
          return IconThemeData(
            color: selected ? VoxColors.primary : VoxColors.textMuted,
          );
        }),
      ),
      dividerTheme: const DividerThemeData(color: VoxColors.border, space: 1),
    );
  }

  // ── Modo escuro (fundo bem preto, como no site) ────────────
  static ThemeData get dark {
    const bg = Color(0xFF000000);
    const surface = Color(0xFF121212);
    const surfaceInput = Color(0xFF0A0A0A);
    const border = Color(0xFF2A2A2A);
    const textMain = Color(0xFFE6E8EC);
    const textMuted = Color(0xFF9AA0AA);

    final base = ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,
      colorScheme: ColorScheme.fromSeed(
        seedColor: VoxColors.accent,
        brightness: Brightness.dark,
        primary: VoxColors.accent,
        secondary: VoxColors.secondary,
        error: VoxColors.error,
        surface: surface,
      ),
      scaffoldBackgroundColor: bg,
      fontFamily: GoogleFonts.inter().fontFamily,
      textTheme: GoogleFonts.interTextTheme(
        ThemeData.dark().textTheme,
      ).apply(bodyColor: textMain, displayColor: textMain),
    );

    return base.copyWith(
      cardTheme: CardThemeData(
        color: surface,
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12),
          side: const BorderSide(color: border),
        ),
        margin: EdgeInsets.zero,
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: surfaceInput,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(10),
          borderSide: const BorderSide(color: border, width: 1.5),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(10),
          borderSide: const BorderSide(color: border, width: 1.5),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(10),
          borderSide: const BorderSide(color: VoxColors.accent, width: 2),
        ),
        labelStyle: const TextStyle(
          color: textMuted,
          fontWeight: FontWeight.w600,
        ),
        contentPadding: const EdgeInsets.symmetric(
          horizontal: 14,
          vertical: 12,
        ),
      ),
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: surface,
        indicatorColor: VoxColors.accent.withValues(alpha: 0.24),
        labelTextStyle: WidgetStateProperty.resolveWith((states) {
          final selected = states.contains(WidgetState.selected);
          return TextStyle(
            fontSize: 11,
            fontWeight: selected ? FontWeight.w700 : FontWeight.w500,
            color: selected ? Colors.white : textMuted,
          );
        }),
        iconTheme: WidgetStateProperty.resolveWith((states) {
          final selected = states.contains(WidgetState.selected);
          return IconThemeData(color: selected ? Colors.white : textMuted);
        }),
      ),
      dividerTheme: const DividerThemeData(color: border, space: 1),
    );
  }

  // ── Alto contraste (preto/branco/amarelo) ──────────────────
  static ThemeData get highContrast {
    const bg = Color(0xFF000000);
    const yellow = Color(0xFFFFFF00);

    final base = ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,
      colorScheme: const ColorScheme.dark(
        primary: yellow,
        onPrimary: Colors.black,
        secondary: yellow,
        surface: Colors.black,
        onSurface: Colors.white,
        error: Color(0xFFFF5555),
      ),
      scaffoldBackgroundColor: bg,
      fontFamily: GoogleFonts.inter().fontFamily,
      textTheme: GoogleFonts.interTextTheme(
        ThemeData.dark().textTheme,
      ).apply(bodyColor: Colors.white, displayColor: Colors.white),
    );

    return base.copyWith(
      cardTheme: CardThemeData(
        color: Colors.black,
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12),
          side: const BorderSide(color: Colors.white, width: 2),
        ),
        margin: EdgeInsets.zero,
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          backgroundColor: yellow,
          foregroundColor: Colors.black,
          textStyle: const TextStyle(fontWeight: FontWeight.w800, fontSize: 15),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(10),
          ),
        ),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: yellow,
          foregroundColor: Colors.black,
          elevation: 0,
          textStyle: const TextStyle(fontWeight: FontWeight.w800, fontSize: 15),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(10),
          ),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: Colors.black,
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(10),
          borderSide: const BorderSide(color: Colors.white, width: 2),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(10),
          borderSide: const BorderSide(color: Colors.white, width: 2),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(10),
          borderSide: const BorderSide(color: yellow, width: 2),
        ),
        labelStyle: const TextStyle(
          color: Colors.white,
          fontWeight: FontWeight.w700,
        ),
      ),
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: Colors.black,
        indicatorColor: yellow.withValues(alpha: 0.3),
        labelTextStyle: WidgetStateProperty.all(
          const TextStyle(
            fontSize: 11,
            fontWeight: FontWeight.w700,
            color: Colors.white,
          ),
        ),
        iconTheme: WidgetStateProperty.all(
          const IconThemeData(color: Colors.white),
        ),
      ),
      dividerTheme: const DividerThemeData(color: Colors.white, space: 1),
    );
  }

  /// Matriz de cor para simular daltonismo (5x4, formato do ColorFilter).
  /// Retorna null para modos que não precisam de filtro.
  static List<double>? colorMatrixFor(AccessibilityMode mode) {
    switch (mode) {
      case AccessibilityMode.protanopia:
        return const [
          0.567, 0.433, 0.0, 0, 0, //
          0.558, 0.442, 0.0, 0, 0, //
          0.0, 0.242, 0.758, 0, 0, //
          0, 0, 0, 1, 0, //
        ];
      case AccessibilityMode.deuteranopia:
        return const [
          0.625, 0.375, 0.0, 0, 0, //
          0.70, 0.30, 0.0, 0, 0, //
          0.0, 0.30, 0.70, 0, 0, //
          0, 0, 0, 1, 0, //
        ];
      case AccessibilityMode.tritanopia:
        return const [
          0.95, 0.05, 0.0, 0, 0, //
          0.0, 0.433, 0.567, 0, 0, //
          0.0, 0.475, 0.525, 0, 0, //
          0, 0, 0, 1, 0, //
        ];
      default:
        return null;
    }
  }
}
