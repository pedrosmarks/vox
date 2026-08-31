import 'package:flutter/material.dart';
import '../models/user_profile.dart';
import '../services/auth_service.dart';
import '../theme/vox_app_bar.dart';

/// Tela do administrador para CRUD de moderadores e vereadores, equivalente
/// a usuarios.component.ts do site.
class UsuariosScreen extends StatefulWidget {
  const UsuariosScreen({super.key});

  @override
  State<UsuariosScreen> createState() => _UsuariosScreenState();
}

class _UsuariosScreenState extends State<UsuariosScreen>
    with SingleTickerProviderStateMixin {
  final _authService = AuthService();
  late final TabController _tabController;

  List<UserProfile> _users = [];
  bool _isLoading = true;
  String? _error;

  String get _activeRole =>
      _tabController.index == 0 ? 'MODERATOR' : 'COUNCILOR';
  String get _roleLabel =>
      _activeRole == 'MODERATOR' ? 'Moderador' : 'Vereador';

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    _tabController.addListener(() {
      if (!_tabController.indexIsChanging) _load();
    });
    _load();
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });
    try {
      final users = await _authService.getUsersByRole(_activeRole);
      if (mounted) setState(() => _users = users);
    } catch (_) {
      if (mounted) setState(() => _error = 'Erro ao carregar usuários.');
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  Future<void> _openForm({UserProfile? editing}) async {
    final nameCtrl = TextEditingController(text: editing?.name ?? '');
    final emailCtrl = TextEditingController(text: editing?.email ?? '');
    final cpfCtrl = TextEditingController(text: editing?.cpf ?? '');
    final phoneCtrl = TextEditingController(text: editing?.phone ?? '');
    final passwordCtrl = TextEditingController();
    final birthCtrl = TextEditingController(text: editing?.birthDate ?? '');
    String? errorText;

    await showDialog<void>(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setDialogState) => AlertDialog(
          title: Text('${editing != null ? 'Editar' : 'Novo'} $_roleLabel'),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                if (errorText != null)
                  Padding(
                    padding: const EdgeInsets.only(bottom: 8),
                    child: Text(
                      errorText!,
                      style: const TextStyle(color: Colors.red),
                    ),
                  ),
                TextField(
                  controller: nameCtrl,
                  decoration: const InputDecoration(labelText: 'Nome'),
                ),
                TextField(
                  controller: emailCtrl,
                  decoration: const InputDecoration(labelText: 'E-mail'),
                ),
                TextField(
                  controller: cpfCtrl,
                  decoration: const InputDecoration(labelText: 'CPF'),
                ),
                TextField(
                  controller: phoneCtrl,
                  decoration: const InputDecoration(labelText: 'Telefone'),
                ),
                TextField(
                  controller: birthCtrl,
                  decoration: const InputDecoration(
                    labelText: 'Data de nascimento (AAAA-MM-DD)',
                  ),
                ),
                if (editing == null)
                  TextField(
                    controller: passwordCtrl,
                    obscureText: true,
                    decoration: const InputDecoration(labelText: 'Senha'),
                  ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text('Cancelar'),
            ),
            FilledButton(
              onPressed: () async {
                if (nameCtrl.text.trim().isEmpty ||
                    emailCtrl.text.trim().isEmpty ||
                    cpfCtrl.text.trim().isEmpty) {
                  setDialogState(
                    () => errorText = 'Preencha os campos obrigatórios.',
                  );
                  return;
                }
                if (editing == null && passwordCtrl.text.trim().isEmpty) {
                  setDialogState(
                    () => errorText = 'Informe uma senha para o novo usuário.',
                  );
                  return;
                }
                final municipalityId = await _authService.getMunicipalityId();
                try {
                  if (editing != null) {
                    await _authService.updateUser(editing.id, {
                      'name': nameCtrl.text.trim(),
                      'email': emailCtrl.text.trim(),
                      'cpf': cpfCtrl.text.trim(),
                      'phone': phoneCtrl.text.trim(),
                      'birthDate': birthCtrl.text.trim(),
                      'role': _activeRole,
                      'municipalityId': municipalityId,
                    });
                  } else {
                    await _authService.createUser({
                      'name': nameCtrl.text.trim(),
                      'email': emailCtrl.text.trim(),
                      'cpf': cpfCtrl.text.trim(),
                      'phone': phoneCtrl.text.trim(),
                      'password': passwordCtrl.text,
                      'birthDate': birthCtrl.text.trim(),
                      'role': _activeRole,
                      'municipalityId': municipalityId,
                      'acceptedTerms': true,
                      'acceptedPrivacyPolicy': true,
                    });
                  }
                  if (context.mounted) Navigator.of(context).pop();
                  _load();
                } catch (_) {
                  setDialogState(
                    () => errorText = 'Erro ao salvar. Verifique os dados.',
                  );
                }
              },
              child: const Text('Salvar'),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _deleteUser(UserProfile user) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Remover usuário'),
        content: Text('Remover ${user.name}? Esta ação não pode ser desfeita.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Cancelar'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(context).pop(true),
            child: const Text('Remover'),
          ),
        ],
      ),
    );
    if (confirmed != true) return;
    try {
      await _authService.deleteUser(user.id);
      if (mounted) setState(() => _users.removeWhere((u) => u.id == user.id));
    } catch (_) {
      // ignora falha na ação
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: VoxAppBar(
        title: 'Gerenciar Usuários',
        bottomHeight: 48,
        bottom: TabBar(
          controller: _tabController,
          tabs: const [
            Tab(text: 'Moderadores'),
            Tab(text: 'Vereadores'),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => _openForm(),
        child: const Icon(Icons.add),
      ),
      body: RefreshIndicator(
        onRefresh: _load,
        child: _isLoading
            ? const Center(child: CircularProgressIndicator())
            : _error != null
            ? Center(child: Text(_error!))
            : _users.isEmpty
            ? ListView(
                children: [
                  const SizedBox(height: 120),
                  Center(child: Text('Nenhum $_roleLabel cadastrado.')),
                ],
              )
            : ListView.separated(
                padding: const EdgeInsets.all(16),
                itemCount: _users.length,
                separatorBuilder: (_, _) => const SizedBox(height: 8),
                itemBuilder: (_, i) {
                  final user = _users[i];
                  return Card(
                    child: ListTile(
                      title: Text(user.name),
                      subtitle: Text(user.email),
                      trailing: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          IconButton(
                            icon: const Icon(Icons.edit_outlined),
                            onPressed: () => _openForm(editing: user),
                          ),
                          IconButton(
                            icon: const Icon(Icons.delete_outline),
                            onPressed: () => _deleteUser(user),
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
      ),
    );
  }
}
