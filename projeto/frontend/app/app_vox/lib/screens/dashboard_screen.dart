import 'package:flutter/material.dart';
import '../services/auth_service.dart';
import 'login_screen.dart';

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({super.key});

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  static const _primary = Color(0xFF1e3a8a);
  static const _accent = Color(0xFF7c3aed);
  static const _secondary = Color(0xFFf59e0b);

  final _authService = AuthService();
  String? _role;
  String? _fullname;
  String? _email;

  @override
  void initState() {
    super.initState();
    _loadUserInfo();
  }

  Future<void> _loadUserInfo() async {
    final token = await _authService.getToken();
    if (token == null) {
      if (mounted) {
        Navigator.of(context).pushReplacement(
          MaterialPageRoute(builder: (_) => const LoginScreen()),
        );
      }
      return;
    }
    final info = _authService.decodeToken(token);
    setState(() {
      _role = info['role'] as String?;
      _fullname = info['fullname'] as String?;
      _email = info['email'] as String?;
    });
  }

  Future<void> _logout() async {
    await _authService.logout();
    if (!mounted) return;
    Navigator.of(context).pushReplacement(
      MaterialPageRoute(builder: (_) => const LoginScreen()),
    );
  }

  String get _roleLabel {
    switch (_role) {
      case 'ADMINISTRATOR':
        return 'Logado como admin';
      case 'MODERATOR':
        return 'Logado como moderador';
      case 'CITIZEN':
        return 'Logado como usuário';
      default:
        return 'Logado (role desconhecido)';
    }
  }

  String get _roleIcon {
    switch (_role) {
      case 'ADMINISTRATOR':
        return '🛡️';
      case 'MODERATOR':
        return '⚖️';
      default:
        return '👤';
    }
  }

  Color get _roleBorderColor {
    switch (_role) {
      case 'ADMINISTRATOR':
        return _primary.withOpacity(0.3);
      case 'MODERATOR':
        return _accent.withOpacity(0.3);
      default:
        return _secondary.withOpacity(0.3);
    }
  }

  Color get _roleBgColor {
    switch (_role) {
      case 'ADMINISTRATOR':
        return _primary.withOpacity(0.08);
      case 'MODERATOR':
        return _accent.withOpacity(0.08);
      default:
        return _secondary.withOpacity(0.08);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            stops: [0.0, 0.6, 1.0],
            colors: [_primary, _accent, _secondary],
          ),
        ),
        child: SafeArea(
          child: Center(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Container(
                width: double.infinity,
                constraints: const BoxConstraints(maxWidth: 420),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(20),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.2),
                      blurRadius: 50,
                      offset: const Offset(0, 25),
                    ),
                  ],
                ),
                padding: const EdgeInsets.all(40),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    // Logo row
                    Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Container(
                          width: 48,
                          height: 48,
                          decoration: BoxDecoration(
                            borderRadius: BorderRadius.circular(12),
                          ),
                          child: ClipRRect(
                            borderRadius: BorderRadius.circular(12),
                            child: Image.asset(
                              'assets/images/logo.png',
                              fit: BoxFit.contain,
                              errorBuilder: (_, __, ___) => const Icon(
                                Icons.record_voice_over,
                                color: _primary,
                                size: 36,
                              ),
                            ),
                          ),
                        ),
                        const SizedBox(width: 12),
                        const Text(
                          'VOX CIDADÃO',
                          style: TextStyle(
                            fontSize: 20,
                            fontWeight: FontWeight.w800,
                            color: _primary,
                            letterSpacing: 1.5,
                          ),
                        ),
                      ],
                    ),

                    const SizedBox(height: 24),

                    // Nome do usuário
                    if (_fullname != null) ...[
                      Text(
                        _fullname!,
                        style: const TextStyle(
                          fontSize: 20,
                          fontWeight: FontWeight.w700,
                          color: Color(0xFF1A1A2E),
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        _email ?? '',
                        style: const TextStyle(
                          fontSize: 14,
                          color: Color(0xFF717182),
                        ),
                      ),
                      const SizedBox(height: 20),
                    ],

                    // Role badge
                    Container(
                      width: double.infinity,
                      padding: const EdgeInsets.symmetric(
                          vertical: 24, horizontal: 32),
                      decoration: BoxDecoration(
                        color: _roleBgColor,
                        border: Border.all(color: _roleBorderColor, width: 2),
                        borderRadius: BorderRadius.circular(16),
                      ),
                      child: Column(
                        children: [
                          Text(
                            _roleIcon,
                            style: const TextStyle(fontSize: 40),
                          ),
                          const SizedBox(height: 8),
                          Text(
                            _roleLabel,
                            style: const TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.w700,
                              color: Color(0xFF1A1A2E),
                            ),
                          ),
                        ],
                      ),
                    ),

                    const SizedBox(height: 16),

                    // Role detail
                    if (_role != null)
                      Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          const Text(
                            'Role do JWT: ',
                            style: TextStyle(
                                fontSize: 14, color: Color(0xFF717182)),
                          ),
                          Container(
                            padding: const EdgeInsets.symmetric(
                                horizontal: 8, vertical: 3),
                            decoration: BoxDecoration(
                              color: const Color(0xFFF3F3F5),
                              border: Border.all(
                                  color: Colors.black12, width: 1),
                              borderRadius: BorderRadius.circular(6),
                            ),
                            child: Text(
                              _role!,
                              style: const TextStyle(
                                fontSize: 13,
                                fontFamily: 'monospace',
                                color: _accent,
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                          ),
                        ],
                      ),

                    const SizedBox(height: 24),

                    // Botão logout
                    SizedBox(
                      width: double.infinity,
                      height: 52,
                      child: OutlinedButton(
                        onPressed: _logout,
                        style: OutlinedButton.styleFrom(
                          foregroundColor: const Color(0xFFBE123C),
                          side: const BorderSide(
                              color: Color(0xFFFECDD3), width: 2),
                          backgroundColor: Colors.white,
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(10),
                          ),
                        ),
                        child: const Text(
                          'Sair da conta',
                          style: TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
