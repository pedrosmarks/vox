import 'package:flutter/material.dart';
import 'screens/splash_screen.dart';
import 'services/settings_service.dart';
import 'theme/vox_theme.dart';

void main() {
  runApp(const MainApp());
}

class MainApp extends StatelessWidget {
  const MainApp({super.key});

  @override
  Widget build(BuildContext context) {
    return ValueListenableBuilder<UserSettings>(
      valueListenable: SettingsController.instance,
      builder: (context, settings, _) {
        // 16px é a base do app; a escala de fonte é relativa a isso.
        final scale = settings.fontSize / 16.0;
        final colorMatrix = VoxTheme.colorMatrixFor(settings.mode);

        return MaterialApp(
          debugShowCheckedModeBanner: false,
          theme: VoxTheme.forMode(settings.mode),
          home: const SplashScreen(),
          builder: (context, child) {
            Widget content = child ?? const SizedBox.shrink();

            // Filtro de daltonismo aplicado à tela inteira.
            if (colorMatrix != null) {
              content = ColorFiltered(
                colorFilter: ColorFilter.matrix(colorMatrix),
                child: content,
              );
            }

            // Escala de fonte global.
            final mq = MediaQuery.of(context);
            return MediaQuery(
              data: mq.copyWith(textScaler: TextScaler.linear(scale)),
              child: content,
            );
          },
        );
      },
    );
  }
}
