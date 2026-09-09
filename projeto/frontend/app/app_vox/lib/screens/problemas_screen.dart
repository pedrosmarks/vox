import 'package:flutter/material.dart';
import '../models/issue.dart';
import '../services/auth_service.dart';
import '../services/issue_service.dart';
import '../theme/vox_app_bar.dart';
import '../theme/vox_badges.dart';
import '../utils/status_labels.dart';
import 'problema_detalhe_screen.dart';

/// Tela do vereador para acompanhar e assumir ocorrências relatadas pelos
/// cidadãos do município.
class ProblemasScreen extends StatefulWidget {
  const ProblemasScreen({super.key});

  @override
  State<ProblemasScreen> createState() => _ProblemasScreenState();
}

class _ProblemasScreenState extends State<ProblemasScreen> {
  final _issueService = IssueService();
  final _authService = AuthService();

  List<IssueReport> _issues = [];
  int? _myId;
  int _tab = 0; // 0 = Minhas, 1 = Disponíveis
  int? _linkingId;
  bool _isLoading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });
    try {
      _myId = await _authService.getUserId();
      final issues = await _issueService.getIssues();
      if (mounted) setState(() => _issues = issues);
    } catch (_) {
      if (mounted) {
        setState(() => _error = 'Erro ao carregar problemas relatados.');
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  bool _isMine(IssueReport i) => _myId != null && i.councilorId == _myId;

  List<IssueReport> get _myIssues => _issues.where((i) => _isMine(i)).toList();

  List<IssueReport> get _availableIssues => _issues
      .where((i) => i.councilorId == null || i.councilorId != _myId)
      .toList();

  List<IssueReport> get _visible => _tab == 0 ? _myIssues : _availableIssues;

  Future<void> _toggleLink(IssueReport issue) async {
    if (_myId == null || _linkingId == issue.id) return;
    setState(() => _linkingId = issue.id);
    final target = _isMine(issue) ? null : _myId;
    try {
      await _issueService.assignCouncilor(issue, target);
      await _load();
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Não foi possível atualizar o vínculo.'),
          ),
        );
      }
    } finally {
      if (mounted) setState(() => _linkingId = null);
    }
  }

  Future<void> _openDetail(IssueReport issue) async {
    await Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => ProblemaDetalheScreen(issueId: issue.id),
      ),
    );
    _load(); // recarrega ao voltar (status/vínculo podem ter mudado)
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const VoxAppBar(title: 'Problemas Relatados'),
      body: Column(
        children: [
          _buildTabs(),
          Expanded(
            child: RefreshIndicator(onRefresh: _load, child: _buildBody()),
          ),
        ],
      ),
    );
  }

  Widget _buildTabs() {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 4),
      child: SegmentedButton<int>(
        segments: [
          ButtonSegment(value: 0, label: Text('Minhas (${_myIssues.length})')),
          ButtonSegment(
            value: 1,
            label: Text('Disponíveis (${_availableIssues.length})'),
          ),
        ],
        selected: {_tab},
        onSelectionChanged: (s) => setState(() => _tab = s.first),
      ),
    );
  }

  Widget _buildBody() {
    if (_isLoading) {
      return const Center(child: CircularProgressIndicator());
    }
    if (_error != null) {
      return ListView(
        children: [
          const SizedBox(height: 120),
          Center(child: Text(_error!)),
        ],
      );
    }
    final list = _visible;
    if (list.isEmpty) {
      return ListView(
        children: [
          const SizedBox(height: 120),
          Center(
            child: Text(
              _tab == 0
                  ? 'Você ainda não assumiu nenhuma ocorrência.'
                  : 'Nenhuma ocorrência disponível no momento.',
            ),
          ),
        ],
      );
    }
    return ListView.separated(
      padding: const EdgeInsets.all(16),
      itemCount: list.length,
      separatorBuilder: (_, _) => const SizedBox(height: 8),
      itemBuilder: (_, idx) {
        final issue = list[idx];
        final mine = _isMine(issue);
        return Card(
          child: InkWell(
            borderRadius: BorderRadius.circular(12),
            onTap: () => _openDetail(issue),
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Expanded(
                        child: Text(
                          issue.title,
                          style: Theme.of(context).textTheme.titleMedium,
                        ),
                      ),
                      VoxBadgeColors.issueStatus(
                        issue.status,
                        StatusLabels.issue(issue.status),
                      ),
                    ],
                  ),
                  const SizedBox(height: 4),
                  Text(
                    issue.description,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    style: Theme.of(context).textTheme.bodySmall,
                  ),
                  const SizedBox(height: 10),
                  Row(
                    children: [
                      Expanded(
                        child: OutlinedButton.icon(
                          onPressed: _linkingId == issue.id
                              ? null
                              : () => _toggleLink(issue),
                          icon: Icon(
                            mine ? Icons.check_circle : Icons.how_to_reg,
                            size: 18,
                          ),
                          label: Text(
                            _linkingId == issue.id
                                ? '...'
                                : (mine ? 'Vinculada a mim' : 'Assumir'),
                          ),
                        ),
                      ),
                      const SizedBox(width: 8),
                      TextButton(
                        onPressed: () => _openDetail(issue),
                        child: const Text('Detalhes'),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        );
      },
    );
  }
}
