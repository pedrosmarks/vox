import 'package:flutter/material.dart';
import 'dart:async';
import '../services/auth_service.dart';
import '../theme/vox_colors.dart';
import 'home_shell.dart';
import 'login_screen.dart';

class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key});

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> {
  final _authService = AuthService();

  @override
  void initState() {
    super.initState();
    Timer(const Duration(seconds: 2), () async {
      final loggedIn = await _authService.isLoggedIn();
      if (!mounted) return;
      Navigator.of(context).pushReplacement(
        MaterialPageRoute(
          builder: (context) =>
              loggedIn ? const HomeShell() : const LoginScreen(),
        ),
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: VoxColors.primary,
      body: Center(
        child: Image.asset('assets/images/logo2.jpeg', width: 160, height: 160),
      ),
    );
  }
}
