import 'package:flutter/material.dart';
import '../models/user_profile.dart';
import '../services/auth_service.dart';
import '../theme/vox_app_bar.dart';
import '../theme/vox_badges.dart';
import 'login_screen.dart';
import 'moderacao_screen.dart';

class PerfilScreen extends StatefulWidget {
  const PerfilScreen({super.key});

  @override
  State<PerfilScreen> createState() => _PerfilScreenState();
}

class _PerfilScreenState extends State<PerfilScreen> {
  final _authService = AuthService();

  UserProfile? _user;
  bool _isLoading = true;
  String? _loadError;

  final _nameController = TextEditingController();
  final _phoneController = TextEditingController();
  bool _isSavingProfile = false;
  String? _profileMessage;
  bool _profileSuccess = false;

  final _currentPasswordController = TextEditingController();
  final _newPasswordController = TextEditingController();
  final _confirmPasswordController = TextEditingController();
  bool _isSavingPassword = false;
  String? _passwordMessage;
  bool _passwordSuccess = false;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _isLoading = true;
      _loadError = null;
    });
    try {
      final user = await _authService.fetchCurrentUser();
      _user = user;
      _nameController.text = user.name;
      _phoneController.text = user.phone ?? '';
    } catch (_) {
      _loadError = 'Não foi possível carregar os dados do perfil.';
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  String _roleLabel(String role) {
    const map = {
      'ADMINISTRATOR': 'Administrador',
      'MODERATOR': 'Moderador',
      'CITIZEN': 'Cidadão',
    };
    return map[role] ?? role;
  }

  Future<void> _saveProfile() async {
    if (_user == null) return;
    if (_nameController.text.trim().isEmpty) {
      setState(() {
        _profileSuccess = false;
        _profileMessage = 'O nome não pode ficar em branco.';
      });
      return;
    }
    setState(() {
      _isSavingProfile = true;
      _profileMessage = null;
    });
    try {
      await _authService.updateProfile(_user!.id, {
        'name': _nameController.text.trim(),
        if (_phoneController.text.trim().isNotEmpty)
          'phone': _phoneController.text.trim(),
      });
      _profileSuccess = true;
      _profileMessage = 'Dados atualizados com sucesso!';
      _load();
    } catch (_) {
      _profileSuccess = false;
      _profileMessage = 'Erro ao salvar. Tente novamente.';
    } finally {
      if (mounted) setState(() => _isSavingProfile = false);
    }
  }

  Future<void> _savePassword() async {
    setState(() {
      _passwordMessage = null;
      _passwordSuccess = false;
    });

    if (_currentPasswordController.text.isEmpty ||
        _newPasswordController.text.isEmpty ||
        _confirmPasswordController.text.isEmpty) {
      setState(() => _passwordMessage = 'Preencha todos os campos.');
      return;
    }
    if (_newPasswordController.text.length < 6) {
      setState(
        () =>
            _passwordMessage = 'A nova senha deve ter pelo menos 6 caracteres.',
      );
      return;
    }
    if (_newPasswordController.text != _confirmPasswordController.text) {
      setState(() => _passwordMessage = 'As senhas não coincidem.');
      return;
    }

    setState(() => _isSavingPassword = true);
    try {
      await _authService.updatePassword(
        _currentPasswordController.text,
        _newPasswordController.text,
      );
      _passwordSuccess = true;
      _passwordMessage = 'Senha alterada com sucesso!';
      _currentPasswordController.clear();
      _newPasswordController.clear();
      _confirmPasswordController.clear();
    } catch (_) {
      _passwordSuccess = false;
      _passwordMessage = 'Erro ao alterar senha. Tente novamente.';
    } finally {
      if (mounted) setState(() => _isSavingPassword = false);
    }
  }

  Future<void> _logout() async {
    await _authService.logout();
    if (!mounted) return;
    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (_) => const LoginScreen()),
      (route) => false,
    );
  }

  @override
  void dispose() {
    _nameController.dispose();
    _phoneController.dispose();
    _currentPasswordController.dispose();
    _newPasswordController.dispose();
    _confirmPasswordController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Perfil'),
        actions: [
          IconButton(
            onPressed: _logout,
            icon: const Icon(Icons.logout),
            tooltip: 'Sair',
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _loadError != null
          ? Center(child: Text(_loadError!))
          : ListView(
              padding: const EdgeInsets.all(16),
              children: [
                Center(
                  child: Column(
                    children: [
                      CircleAvatar(
                        radius: 36,
                        child: Text(
                          _user!.name.isNotEmpty
                              ? _user!.name[0].toUpperCase()
                              : '?',
                          style: const TextStyle(fontSize: 28),
                        ),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        _user!.name,
                        style: Theme.of(context).textTheme.titleLarge,
                      ),
                      Text(_user!.email),
                      const SizedBox(height: 4),
                      VoxBadgeColors.role(_user!.role, _roleLabel(_user!.role)),
                    ],
                  ),
                ),
                if (_user!.role == 'MODERATOR' ||
                    _user!.role == 'ADMINISTRATOR') ...[
                  const SizedBox(height: 16),
                  OutlinedButton.icon(
                    onPressed: () => Navigator.of(context).push(
                      MaterialPageRoute(
                        builder: (_) => const ModeracaoScreen(),
                      ),
                    ),
                    icon: const Icon(Icons.gavel_outlined),
                    label: const Text('Moderação'),
                  ),
                ],
                const SizedBox(height: 24),
                Text(
                  'Dados pessoais',
                  style: Theme.of(context).textTheme.titleMedium,
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: _nameController,
                  decoration: const InputDecoration(labelText: 'Nome'),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: _phoneController,
                  decoration: const InputDecoration(labelText: 'Telefone'),
                  keyboardType: TextInputType.phone,
                ),
                if (_profileMessage != null)
                  Padding(
                    padding: const EdgeInsets.only(top: 8),
                    child: Text(
                      _profileMessage!,
                      style: TextStyle(
                        color: _profileSuccess ? Colors.green : Colors.red,
                      ),
                    ),
                  ),
                const SizedBox(height: 12),
                FilledButton(
                  onPressed: _isSavingProfile ? null : _saveProfile,
                  child: _isSavingProfile
                      ? const SizedBox(
                          height: 20,
                          width: 20,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Text('Salvar dados'),
                ),
                const Divider(height: 40),
                Text(
                  'Alterar senha',
                  style: Theme.of(context).textTheme.titleMedium,
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: _currentPasswordController,
                  decoration: const InputDecoration(labelText: 'Senha atual'),
                  obscureText: true,
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: _newPasswordController,
                  decoration: const InputDecoration(labelText: 'Nova senha'),
                  obscureText: true,
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: _confirmPasswordController,
                  decoration: const InputDecoration(
                    labelText: 'Confirmar nova senha',
                  ),
                  obscureText: true,
                ),
                if (_passwordMessage != null)
                  Padding(
                    padding: const EdgeInsets.only(top: 8),
                    child: Text(
                      _passwordMessage!,
                      style: TextStyle(
                        color: _passwordSuccess ? Colors.green : Colors.red,
                      ),
                    ),
                  ),
                const SizedBox(height: 12),
                FilledButton(
                  onPressed: _isSavingPassword ? null : _savePassword,
                  child: _isSavingPassword
                      ? const SizedBox(
                          height: 20,
                          width: 20,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Text('Alterar senha'),
                ),
              ],
            ),
    );
  }
}
