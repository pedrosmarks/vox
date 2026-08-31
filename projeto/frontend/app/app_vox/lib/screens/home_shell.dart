import 'package:flutter/material.dart';
import '../services/auth_service.dart';
import 'projetos_screen.dart';
import 'sugestoes_screen.dart';
import 'relatar_problema_screen.dart';
import 'problemas_screen.dart';
import 'comunidade_screen.dart';
import 'audiencia_screen.dart';
import 'moderacao_screen.dart';
import 'usuarios_screen.dart';
import 'logs_screen.dart';
import 'perfil_screen.dart';

/// Shell com navegação por abas, equivalente ao navbar do site Angular —
/// inclusive nas regras por papel (navbar.component.html): CITIZEN vê
/// Sugestões/Relatar Problema; COUNCILOR vê Problemas Relatados;
/// MODERATOR vê Moderação; ADMINISTRATOR NÃO acessa Projetos/Audiência/
/// Comunidade/Moderação — só vê Usuários, Logs e Perfil.
class HomeShell extends StatefulWidget {
  const HomeShell({super.key});

  @override
  State<HomeShell> createState() => _HomeShellState();
}

class _HomeShellState extends State<HomeShell> {
  final _authService = AuthService();
  int _index = 0;
  String? _role;
  bool _isLoading = true;

  bool get _isAdmin => _role == 'ADMINISTRATOR';
  bool get _isModerator => _role == 'MODERATOR' || _role == 'ADMINISTRATOR';
  bool get _isCouncilor => _role == 'COUNCILOR';

  @override
  void initState() {
    super.initState();
    _loadRole();
  }

  Future<void> _loadRole() async {
    final role = await _authService.getUserRole();
    if (!mounted) return;
    setState(() {
      _role = role;
      _isLoading = false;
    });
  }

  List<Widget> get _screens {
    if (_isAdmin) {
      return const [UsuariosScreen(), LogsScreen(), PerfilScreen()];
    }
    if (_isModerator) {
      return const [
        ProjetosScreen(),
        ComunidadeScreen(),
        AudienciaScreen(),
        ModeracaoScreen(),
        PerfilScreen(),
      ];
    }
    if (_isCouncilor) {
      return const [
        ProjetosScreen(),
        ProblemasScreen(),
        ComunidadeScreen(),
        AudienciaScreen(),
        PerfilScreen(),
      ];
    }
    return const [
      ProjetosScreen(),
      SugestoesScreen(),
      RelatarProblemaScreen(),
      ComunidadeScreen(),
      AudienciaScreen(),
      PerfilScreen(),
    ];
  }

  List<NavigationDestination> get _destinations {
    const projetos = NavigationDestination(
      icon: Icon(Icons.home_work_outlined),
      selectedIcon: Icon(Icons.home_work),
      label: 'Projetos',
    );
    const comunidade = NavigationDestination(
      icon: Icon(Icons.groups_outlined),
      selectedIcon: Icon(Icons.groups),
      label: 'Comunidade',
    );
    const audiencia = NavigationDestination(
      icon: Icon(Icons.videocam_outlined),
      selectedIcon: Icon(Icons.videocam),
      label: 'Audiência',
    );
    const perfil = NavigationDestination(
      icon: Icon(Icons.person_outline),
      selectedIcon: Icon(Icons.person),
      label: 'Perfil',
    );

    if (_isAdmin) {
      return [
        const NavigationDestination(
          icon: Icon(Icons.badge_outlined),
          selectedIcon: Icon(Icons.badge),
          label: 'Usuários',
        ),
        const NavigationDestination(
          icon: Icon(Icons.receipt_long_outlined),
          selectedIcon: Icon(Icons.receipt_long),
          label: 'Logs',
        ),
        perfil,
      ];
    }
    if (_isModerator) {
      return [
        projetos,
        comunidade,
        audiencia,
        const NavigationDestination(
          icon: Icon(Icons.gavel_outlined),
          selectedIcon: Icon(Icons.gavel),
          label: 'Moderação',
        ),
        perfil,
      ];
    }
    if (_isCouncilor) {
      return [
        projetos,
        const NavigationDestination(
          icon: Icon(Icons.report_problem_outlined),
          selectedIcon: Icon(Icons.report_problem),
          label: 'Problemas',
        ),
        comunidade,
        audiencia,
        perfil,
      ];
    }
    return [
      projetos,
      const NavigationDestination(
        icon: Icon(Icons.lightbulb_outline),
        selectedIcon: Icon(Icons.lightbulb),
        label: 'Sugestões',
      ),
      const NavigationDestination(
        icon: Icon(Icons.report_problem_outlined),
        selectedIcon: Icon(Icons.report_problem),
        label: 'Problemas',
      ),
      comunidade,
      audiencia,
      perfil,
    ];
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }
    return Scaffold(
      body: IndexedStack(index: _index, children: _screens),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _index,
        onDestinationSelected: (i) => setState(() => _index = i),
        destinations: _destinations,
      ),
    );
  }
}
