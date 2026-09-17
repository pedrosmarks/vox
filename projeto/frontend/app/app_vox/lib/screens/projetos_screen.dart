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

enum _Filter { todos, oficiais, sugeridos, apoiados }

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
  final Map<int, int> _approvals = {};
  final Set<int> _supported = {};
  final Set<int> _supporting = {};
  bool _isCitizen = false;
  final Map<int, int> _signatureCounts = {};
  final Set<int> _signed = {};
  final Set<int> _signing = {};
  _Filter _filter = _Filter.todos;
  bool _isLoading = true;
  String? _error;

  Future<void> _loadSignatures() async {
    for (final p in _all) {
      try {
        final count = await _projectService.getSignatureCount(p.id);
        if (!mounted) return;
        setState(() => _signatureCounts[p.id] = count);
      } catch (_) {}
      if (_isCitizen) {
        try {
          final signed = await _projectService.hasSignedProject(p.id);
          if (!mounted) return;
          setState(() {
            if (signed) {
              _signed.add(p.id);
            } else {
              _signed.remove(p.id);
            }
          });
        } catch (_) {}
      }
    }
  }

  int _signatureCount(int id) => _signatureCounts[id] ?? 0;

  Future<void> _toggleSign(int id) async {
    if (_signing.contains(id)) return;
    final wasSigned = _signed.contains(id);
    setState(() {
      _signing.add(id);
      if (wasSigned) {
        _signed.remove(id);
        _signatureCounts[id] = (_signatureCount(id) - 1).clamp(0, 1 << 30);
      } else {
        _signed.add(id);
        _signatureCounts[id] = _signatureCount(id) + 1;
      }
    });
    try {
      if (wasSigned) {
        await _projectService.unsignProject(id);
      } else {
        await _projectService.signProject(id);
      }
    } catch (_) {
      if (mounted) {
        setState(() {
          if (wasSigned) {
            _signed.add(id);
            _signatureCounts[id] = _signatureCount(id) + 1;
          } else {
            _signed.remove(id);
            _signatureCounts[id] = (_signatureCount(id) - 1).clamp(0, 1 << 30);
          }
        });
      }
    } finally {
      if (mounted) setState(() => _signing.remove(id));
    }
  }

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
    // Projetos rejeitados, cancelados ou ainda em análise ficam fora do
    // catálogo público; aparecem apenas na fila de moderação.
    return p.status != 'REJECTED' &&
        p.status != 'CANCELLED' &&
        p.status != 'PENDING_APPROVAL' &&
        p.status != 'IN_ANALYSIS';
  }

  Future<void> _load() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });
    try {
      _isCitizen = (await _authService.getUserRole()) == 'CITIZEN';
      final projects = await _projectService.getProjects();
      _all = projects.where(_isVisible).toList();
      _applyFilter();
      unawaited(_loadAuthorNames());
      unawaited(_loadApprovals());
      unawaited(_loadSignatures());
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

  /// Carrega a contagem de apoios de cada projeto e, para cidadão, se já apoiou.
  Future<void> _loadApprovals() async {
    for (final p in _all) {
      try {
        final count = await _projectService.getApprovalCount(p.id);
        if (!mounted) return;
        setState(() {
          _approvals[p.id] = count;
          if (_filter == _Filter.apoiados) _applyFilter();
        });
      } catch (_) {
        // ignora falha individual
      }
      if (_isCitizen) {
        final op = await _projectService.getMyOpinion(p.id);
        if (!mounted) return;
        setState(() {
          if (op == 'APPROVE') {
            _supported.add(p.id);
          } else {
            _supported.remove(p.id);
          }
        });
      }
    }
  }

  int _approvalCount(int id) => _approvals[id] ?? 0;

  /// Apoia (APPROVE) ou remove o apoio (NEUTRAL) do projeto.
  Future<void> _toggleSupport(int id) async {
    if (_supporting.contains(id)) return;
    final wasSupported = _supported.contains(id);
    setState(() {
      _supporting.add(id);
      if (wasSupported) {
        _supported.remove(id);
        _approvals[id] = (_approvalCount(id) - 1).clamp(0, 1 << 30);
      } else {
        _supported.add(id);
        _approvals[id] = _approvalCount(id) + 1;
      }
      if (_filter == _Filter.apoiados) _applyFilter();
    });
    try {
      await _projectService.setOpinion(
        id,
        wasSupported ? 'NEUTRAL' : 'APPROVE',
      );
    } catch (_) {
      // Reverte em caso de falha.
      if (mounted) {
        setState(() {
          if (wasSupported) {
            _supported.add(id);
            _approvals[id] = _approvalCount(id) + 1;
          } else {
            _supported.remove(id);
            _approvals[id] = (_approvalCount(id) - 1).clamp(0, 1 << 30);
          }
        });
      }
    } finally {
      if (mounted) setState(() => _supporting.remove(id));
    }
  }

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
      case _Filter.apoiados:
        _filtered = List.of(
          _all,
        )..sort((a, b) => _approvalCount(b.id).compareTo(_approvalCount(a.id)));
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
                          const SizedBox(width: 8),
                          _filterChip('Mais apoiados', _Filter.apoiados),
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
                                      const SizedBox(height: 6),
                                      Row(
                                        children: [
                                          if (_isCitizen) ...[
                                            const Spacer(),
                                            _supported.contains(p.id)
                                                ? FilledButton(
                                                    onPressed:
                                                        _supporting.contains(
                                                          p.id,
                                                        )
                                                        ? null
                                                        : () => _toggleSupport(
                                                            p.id,
                                                          ),
                                                    child: Text(
                                                      'Apoiado (${_approvalCount(p.id)})',
                                                    ),
                                                  )
                                                : OutlinedButton(
                                                    onPressed:
                                                        _supporting.contains(
                                                          p.id,
                                                        )
                                                        ? null
                                                        : () => _toggleSupport(
                                                            p.id,
                                                          ),
                                                    child: Text(
                                                      'Apoiar (${_approvalCount(p.id)})',
                                                    ),
                                                  ),
                                          ] else
                                            Text(
                                              'Apoios (${_approvalCount(p.id)})',
                                              style: const TextStyle(
                                                fontWeight: FontWeight.w600,
                                              ),
                                            ),
                                          if (_isCitizen) ...[
                                            const SizedBox(width: 8),
                                            _signed.contains(p.id)
                                                ? FilledButton(
                                                    onPressed:
                                                        _signing.contains(p.id)
                                                        ? null
                                                        : () =>
                                                              _toggleSign(p.id),
                                                    child: Text(
                                                      'Assinado (${_signatureCount(p.id)})',
                                                    ),
                                                  )
                                                : OutlinedButton(
                                                    onPressed:
                                                        _signing.contains(p.id)
                                                        ? null
                                                        : () =>
                                                              _toggleSign(p.id),
                                                    child: Text(
                                                      'Assinar (${_signatureCount(p.id)})',
                                                    ),
                                                  ),
                                          ],
                                        ],
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
