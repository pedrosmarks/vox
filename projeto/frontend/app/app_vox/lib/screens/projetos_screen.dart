import 'dart:async';

import 'package:flutter/material.dart';
import '../models/project.dart';
import '../services/auth_service.dart';
import '../services/project_service.dart';
import '../theme/vox_app_bar.dart';
import '../theme/vox_badges.dart';
import '../utils/status_labels.dart';
import 'login_screen.dart';
import 'projeto_detalhe_screen.dart';

enum _Filter { todos, oficiais, sugeridos }

class ProjetosScreen extends StatefulWidget {
  const ProjetosScreen({super.key});

  @override
  State<ProjetosScreen> createState() => _ProjetosScreenState();
}

class _ProjetosScreenState extends State<ProjetosScreen> {
  final _authService = AuthService();
  final _projectService = ProjectService();

  List<Project> _all = [];
  List<Project> _filtered = [];
  final Map<int, String> _authorNames = {};
  _Filter _filter = _Filter.todos;
  bool _isLoading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _init();
  }

  Future<void> _init() async {
    if (!await _authService.isLoggedIn()) {
      _goToLogin();
      return;
    }
    await _load();
  }

  void _goToLogin() {
    if (!mounted) return;
    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (_) => const LoginScreen()),
      (route) => false,
    );
  }

  bool _isVisible(Project p) {
    final isCitizen = !p.isOfficial && p.type == 'CITIZEN';
    return !isCitizen ||
        (p.status != 'PENDING_APPROVAL' && p.status != 'IN_ANALYSIS');
  }

  Future<void> _load() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });
    try {
      final projects = await _projectService.getProjects();
      _all = projects.where(_isVisible).toList();
      _applyFilter();
      unawaited(_loadAuthorNames());
    } catch (_) {
      _error = 'Erro ao carregar projetos. Tente novamente.';
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  Future<void> _loadAuthorNames() async {
    final citizenProjects = _all
        .where((p) => !p.isOfficial && p.type == 'CITIZEN')
        .toList();
    final ids = {for (final p in citizenProjects) p.authorId};
    for (final id in ids) {
      try {
        final user = await _projectService.getUserById(id);
        final name = (user.fullname?.isNotEmpty ?? false)
            ? user.fullname!
            : user.name;
        if (mounted) setState(() => _authorNames[id] = name);
      } catch (_) {
        // ignora falha ao buscar autor individual
      }
    }
  }

  String _authorName(int id) => _authorNames[id] ?? 'Usuário #$id';

  void _applyFilter() {
    switch (_filter) {
      case _Filter.oficiais:
        _filtered = _all
            .where((p) => p.isOfficial || p.type == 'OFFICIAL')
            .toList();
        break;
      case _Filter.sugeridos:
        _filtered = _all
            .where((p) => !p.isOfficial && p.type == 'CITIZEN')
            .toList();
        break;
      case _Filter.todos:
        _filtered = List.of(_all);
        break;
    }
  }

  String _typeLabel(Project p) => p.isOfficial || p.type == 'OFFICIAL'
      ? 'Projeto Oficial'
      : 'Projeto Sugerido';

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const VoxAppBar(title: 'Projetos'),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
          ? _ErrorView(message: _error!, onRetry: _load)
          : RefreshIndicator(
              onRefresh: _load,
              child: Column(
                children: [
                  Padding(
                    padding: const EdgeInsets.all(12),
                    child: SingleChildScrollView(
                      scrollDirection: Axis.horizontal,
                      child: Row(
                        children: [
                          _filterChip('Todos', _Filter.todos),
                          const SizedBox(width: 8),
                          _filterChip('Oficiais', _Filter.oficiais),
                          const SizedBox(width: 8),
                          _filterChip('Sugeridos', _Filter.sugeridos),
                        ],
                      ),
                    ),
                  ),
                  Expanded(
                    child: _filtered.isEmpty
                        ? ListView(
                            children: const [
                              Padding(
                                padding: EdgeInsets.only(top: 80),
                                child: Center(
                                  child: Text('Nenhum projeto encontrado.'),
                                ),
                              ),
                            ],
                          )
                        : ListView.builder(
                            padding: const EdgeInsets.symmetric(horizontal: 12),
                            itemCount: _filtered.length,
                            itemBuilder: (context, index) {
                              final p = _filtered[index];
                              return Card(
                                margin: const EdgeInsets.only(bottom: 12),
                                child: ListTile(
                                  onTap: () => Navigator.of(context).push(
                                    MaterialPageRoute(
                                      builder: (_) =>
                                          ProjetoDetalheScreen(projectId: p.id),
                                    ),
                                  ),
                                  title: Text(
                                    p.title,
                                    style: const TextStyle(
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                                  subtitle: Column(
                                    crossAxisAlignment:
                                        CrossAxisAlignment.start,
                                    children: [
                                      const SizedBox(height: 4),
                                      Text(
                                        p.description,
                                        maxLines: 2,
                                        overflow: TextOverflow.ellipsis,
                                      ),
                                      const SizedBox(height: 6),
                                      Wrap(
                                        spacing: 8,
                                        children: [
                                          VoxBadgeColors.type(
                                            p.isOfficial ||
                                                p.type == 'OFFICIAL',
                                            _typeLabel(p),
                                          ),
                                          VoxBadgeColors.projectStatus(
                                            p.status,
                                            StatusLabels.project(p.status),
                                          ),
                                        ],
                                      ),
                                      if (!p.isOfficial && p.type == 'CITIZEN')
                                        Padding(
                                          padding: const EdgeInsets.only(
                                            top: 4,
                                          ),
                                          child: Text(
                                            'Por ${_authorName(p.authorId)}',
                                            style: Theme.of(
                                              context,
                                            ).textTheme.bodySmall,
                                          ),
                                        ),
                                    ],
                                  ),
                                ),
                              );
                            },
                          ),
                  ),
                ],
              ),
            ),
    );
  }

  Widget _filterChip(String label, _Filter value) {
    return ChoiceChip(
      label: Text(label),
      selected: _filter == value,
      onSelected: (_) => setState(() {
        _filter = value;
        _applyFilter();
      }),
    );
  }
}

class _ErrorView extends StatelessWidget {
  final String message;
  final VoidCallback onRetry;
  const _ErrorView({required this.message, required this.onRetry});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(message),
          const SizedBox(height: 8),
          ElevatedButton(
            onPressed: onRetry,
            child: const Text('Tentar novamente'),
          ),
        ],
      ),
    );
  }
}
