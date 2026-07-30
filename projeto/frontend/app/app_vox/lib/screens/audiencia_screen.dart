import 'package:flutter/material.dart';
import '../theme/vox_app_bar.dart';

/// Tela de audiências públicas (videochamada). Funcionalidade completa
/// (WebRTC) ainda não implementada, assim como no site.
class AudienciaScreen extends StatelessWidget {
  const AudienciaScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const VoxAppBar(title: 'Audiência Pública'),
      body: const Center(
        child: Padding(
          padding: EdgeInsets.all(24),
          child: Text(
            'Logo vai ser implementado',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
            textAlign: TextAlign.center,
          ),
        ),
      ),
    );
  }
}
