import 'package:flutter/material.dart';
import '../models/sala.dart';
import '../services/auth_service.dart';
import '../services/sala_service.dart';
import '../theme/vox_app_bar.dart';
import 'audiencia_sala_screen.dart';

/// Tela de audiências públicas — lista salas reais via /api/salas, com
/// criação (moderador) e entrada (cidadão), equivalente a audiencia.component.ts.
class AudienciaScreen extends StatefulWidget {
  const AudienciaScreen({super.key});

  @override
  State<AudienciaScreen> createState() => _AudienciaScreenState();
}

class _AudienciaScreenState extends State<AudienciaScreen> {
  final _authService = AuthService();
  final _salaService = SalaService();

  List<Sala> _salas = [];
  bool _isLoading = true;
  String? _error;
  bool _isModerator = false;

  @override
  void initState() {
    super.initState();
    _init();
  }

  Future<void> _init() async {
    final role = await _authService.getUserRole();
    _isModerator = role == 'MODERATOR' || role == 'ADMINISTRATOR';
    await _load();
  }

  Future<void> _load() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });
    try {
      final salas = await _salaService.getSalas();
      if (mounted) setState(() => _salas = salas);
    } catch (_) {
      if (mounted)
        setState(() => _error = 'Erro ao carregar salas de audiência.');
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  Future<void> _createSala() async {
    final nameCtrl = TextEditingController();
    final descCtrl = TextEditingController();
    final created = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Nova Sala de Audiência'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: nameCtrl,
              decoration: const InputDecoration(labelText: 'Nome'),
            ),
            TextField(
              controller: descCtrl,
              decoration: const InputDecoration(labelText: 'Descrição'),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Cancelar'),
          ),
          FilledButton(
            onPressed: () async {
              if (nameCtrl.text.trim().isEmpty) return;
              try {
                await _salaService.createSala(
                  nameCtrl.text.trim(),
                  descCtrl.text.trim(),
                );
                if (context.mounted) Navigator.of(context).pop(true);
              } catch (_) {
                if (context.mounted) Navigator.of(context).pop(false);
              }
            },
            child: const Text('Criar'),
          ),
        ],
      ),
    );
    if (created == true) _load();
  }

  Future<void> _encerrarSala(Sala sala) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Encerrar sala'),
        content: Text('Encerrar a sala "${sala.name}"?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Cancelar'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(context).pop(true),
            child: const Text('Encerrar'),
          ),
        ],
      ),
    );
    if (confirmed != true) return;
    try {
      await _salaService.encerrarSala(sala.id);
      _load();
    } catch (_) {
      // ignora falha na ação
    }
  }

  void _entrar(Sala sala) {
    Navigator.of(context).push(
      MaterialPageRoute(builder: (_) => AudienciaSalaScreen(salaId: sala.id)),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const VoxAppBar(title: 'Audiência Pública'),
      floatingActionButton: _isModerator
          ? FloatingActionButton(
              onPressed: _createSala,
              child: const Icon(Icons.add),
            )
          : null,
      body: RefreshIndicator(
        onRefresh: _load,
        child: _isLoading
            ? const Center(child: CircularProgressIndicator())
            : _error != null
            ? Center(child: Text(_error!))
            : _salas.isEmpty
            ? ListView(
                children: const [
                  SizedBox(height: 120),
                  Center(child: Text('Nenhuma sala de audiência disponível.')),
                ],
              )
            : ListView.separated(
                padding: const EdgeInsets.all(16),
                itemCount: _salas.length,
                separatorBuilder: (_, _) => const SizedBox(height: 8),
                itemBuilder: (_, i) {
                  final sala = _salas[i];
                  final isOpen = sala.status == 'OPEN';
                  return Card(
                    child: ListTile(
                      title: Text(sala.name),
                      subtitle: Text(sala.description),
                      leading: Icon(
                        isOpen ? Icons.circle : Icons.stop_circle_outlined,
                        color: isOpen ? Colors.red : Colors.grey,
                        size: 16,
                      ),
                      onTap: isOpen ? () => _entrar(sala) : null,
                      trailing: _isModerator && isOpen
                          ? IconButton(
                              icon: const Icon(Icons.close),
                              onPressed: () => _encerrarSala(sala),
                            )
                          : null,
                    ),
                  );
                },
              ),
      ),
    );
  }
}
