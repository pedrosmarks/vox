import 'package:flutter/material.dart';
import 'screens/splash_screen.dart';
import 'theme/vox_theme.dart';

void main() {
  runApp(const MainApp());
}

class MainApp extends StatelessWidget {
  const MainApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: VoxTheme.light,
      home: const SplashScreen(),
    );
  }
}
